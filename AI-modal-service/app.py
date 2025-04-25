#!/usr/bin/env python
# coding: utf-8

import numpy as np
import tensorflow as tf
import keras
import os
import cv2
import sys
import json
import tempfile
import uuid  # Thêm thư viện uuid để tạo fingerprint ID duy nhất
from flask import Flask, request, jsonify
from tensorflow.keras.models import load_model
from tensorflow.keras.metrics import Metric
from werkzeug.utils import secure_filename

app = Flask(__name__)

# Định nghĩa các đường dẫn
UPLOAD_FOLDER = "temp_uploads"
MODELS_DIR = os.path.dirname(os.path.abspath(__file__))
os.makedirs(UPLOAD_FOLDER, exist_ok=True)


def convert_to_serializable(obj):
    """Chuyển đổi tất cả các kiểu dữ liệu numpy sang kiểu Python chuẩn"""
    if isinstance(obj, dict):
        return {key: convert_to_serializable(value) for key, value in obj.items()}
    elif isinstance(obj, list):
        return [convert_to_serializable(item) for item in obj]
    elif isinstance(obj, tuple):
        return tuple(convert_to_serializable(item) for item in obj)
    elif isinstance(obj, np.integer):
        return int(obj)
    elif isinstance(obj, np.floating):
        return float(obj)
    elif isinstance(obj, np.ndarray):
        return obj.tolist()
    elif isinstance(obj, np.bool_):
        return bool(obj)
    else:
        return obj


@keras.saving.register_keras_serializable()
class IoU(Metric):
    def __init__(self, name="iou", **kwargs):
        super(IoU, self).__init__(name=name, **kwargs)
        self.intersection = self.add_weight(name="intersection", initializer="zeros")
        self.union = self.add_weight(name="union", initializer="zeros")

    def update_state(self, y_true, y_pred, sample_weight=None):
        y_pred = tf.cast(y_pred > 0.5, tf.float32)
        intersection = tf.reduce_sum(y_true * y_pred)
        union = tf.reduce_sum(y_true) + tf.reduce_sum(y_pred) - intersection

        self.intersection.assign_add(intersection)
        self.union.assign_add(union)

    def result(self):
        return self.intersection / (self.union + 1e-6)

    def reset_state(self):
        self.intersection.assign(0.0)
        self.union.assign(0.0)


def load_models(segmentation_model_path_name, recognition_model_path_name):
    """Load the fingerprint recognition and segmentation models"""
    try:
        # Define model paths relative to script location
        script_dir = os.path.dirname(os.path.abspath(__file__))
        recognition_model_path = os.path.join(
            script_dir,
            f"fingerprint_models/recognition/{recognition_model_path_name}.keras",
        )
        segmentation_model_path = os.path.join(
            script_dir,
            f"fingerprint_models/segmentation/{segmentation_model_path_name}.keras",
        )

        # Load models
        recognition_model = load_model(
            recognition_model_path, custom_objects={"IoU": IoU}
        )
        segmentation_model = load_model(
            segmentation_model_path, custom_objects={"IoU": IoU}
        )

        # Get input shapes
        if isinstance(recognition_model.input, list):
            recognition_shape = recognition_model.input[0].shape[1:3]
        else:
            recognition_shape = recognition_model.input_shape[1:3]

        if isinstance(segmentation_model.input, list):
            segmentation_shape = segmentation_model.input[0].shape[1:3]
        else:
            segmentation_shape = segmentation_model.input_shape[1:3]

        return (
            recognition_model,
            segmentation_model,
            recognition_shape,
            segmentation_shape,
        )

    except Exception as e:
        print(f"Error loading models: {e}", file=sys.stderr)
        return None, None, (90, 90), (90, 90)


def preprocess_fingerprint(
    image_path, segmentation_model, recognition_shape, segmentation_shape
):
    """Preprocess fingerprint images to match model requirements"""
    try:
        # Read image
        img = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)
        if img is None:
            raise ValueError(f"Could not load image from {image_path}")

        # Resize for segmentation
        img_for_segmentation = cv2.resize(
            img, (segmentation_shape[1], segmentation_shape[0])
        )

        # Enhance contrast
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
        img_for_segmentation = clahe.apply(img_for_segmentation)

        # Prepare for segmentation model
        img_for_segmentation = (
            np.expand_dims(np.expand_dims(img_for_segmentation, axis=-1), axis=0)
            / 255.0
        )

        # Get segmentation mask
        segmentation_output = segmentation_model.predict(
            img_for_segmentation, verbose=0
        )

        # Process mask
        if isinstance(segmentation_output, list) and len(segmentation_output) > 0:
            mask = segmentation_output[0]
        else:
            mask = np.ones(img.shape, dtype=np.uint8)

        mask = (mask > 0.5).astype(np.uint8)

        # Convert 3D mask to 2D if needed
        if len(mask.shape) == 3:
            mask_2d = mask[:, :, 0]
        else:
            mask_2d = mask

        # Resize mask and apply to image
        mask_resized = cv2.resize(mask_2d, (img.shape[1], img.shape[0]))
        img = img * mask_resized

        # Resize for recognition model
        img = cv2.resize(img, (recognition_shape[1], recognition_shape[0]))

        # Normalize and add channel dimension
        img = img / 255.0
        img = np.expand_dims(img, axis=-1)

        return img

    except Exception as e:
        print(f"Error in preprocess_fingerprint: {e}", file=sys.stderr)
        raise


def create_embedding_model(recognition_model):
    """Create embedding extractor model"""
    feature_extractor = recognition_model.get_layer("functional")
    input_shape = feature_extractor.input_shape[1:]
    new_input = keras.layers.Input(shape=input_shape)
    embedding = feature_extractor(new_input)
    embedding_model = keras.Model(inputs=new_input, outputs=embedding)
    return embedding_model

def load_embeddings_db():
    """Load embeddings database from file"""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    db_dir = os.path.join(script_dir, "fingerprint_adapting_models")
    os.makedirs(db_dir, exist_ok=True)  # Tạo thư mục nếu không tồn tại

    db_path = os.path.join(db_dir, "employee_embeddings.npy")
    fp_db_path = os.path.join(db_dir, "fingerprint_embeddings.npy")

    result = {"employee_embeddings": {}, "fingerprint_embeddings": {}}

    if os.path.exists(db_path):
        try:
            embeddings_db = np.load(db_path, allow_pickle=True).item()
            result["employee_embeddings"] = embeddings_db
        except Exception as e:
            print(f"Error loading employee embeddings database: {e}", file=sys.stderr)
            result["employee_embeddings"] = {}
    else:
        print(
            f"Employee embeddings database not found at {db_path}, creating new database",
            file=sys.stderr,
        )
        # Khởi tạo database trống
        np.save(db_path, {})

    if os.path.exists(fp_db_path):
        try:
            fp_embeddings_db = np.load(fp_db_path, allow_pickle=True).item()
            result["fingerprint_embeddings"] = fp_embeddings_db
        except Exception as e:
            print(
                f"Error loading fingerprint embeddings database: {e}", file=sys.stderr
            )
            result["fingerprint_embeddings"] = {}
    else:
        print(
            f"Fingerprint embeddings database not found at {fp_db_path}, creating new database",
            file=sys.stderr,
        )
        # Khởi tạo database trống
        np.save(fp_db_path, {})

    return result


def recognize_employee(
    image_path,
    embedding_model,
    segmentation_model,
    recognition_shape,
    segmentation_shape,
    threshold=1.0,
    target_fingerprint_id=None,
):
    try:
        embeddings_dbs = load_embeddings_db()

        employee_embeddings_db = embeddings_dbs.get("employee_embeddings", {})
        fingerprint_embeddings_db = embeddings_dbs.get("fingerprint_embeddings", {})

        print("Available fingerprint IDs:", list(fingerprint_embeddings_db.keys()))

        if target_fingerprint_id:
            print(f"Target fingerprint ID: {target_fingerprint_id}")
            print(
                f"Is target in database: {target_fingerprint_id in fingerprint_embeddings_db}"
            )

        if not employee_embeddings_db and not fingerprint_embeddings_db:
            return {"error": "Embeddings database is empty"}

        processed_img = preprocess_fingerprint(
            image_path, segmentation_model, recognition_shape, segmentation_shape
        )
        processed_img = np.expand_dims(processed_img, axis=0)

        embedding = embedding_model.predict(processed_img, verbose=0)[0]

        if target_fingerprint_id and fingerprint_embeddings_db:
            if target_fingerprint_id in fingerprint_embeddings_db:
                target_data = fingerprint_embeddings_db[target_fingerprint_id]
                stored_embedding = target_data["embedding"]
                employee_id = target_data["employee_id"]

                similarity = np.dot(embedding, stored_embedding) / (
                    np.linalg.norm(embedding) * np.linalg.norm(stored_embedding)
                )

                if similarity >= threshold:
                    return {
                        "similarity": {
                            "employee_id": employee_id,
                            "fingerprint_id": target_fingerprint_id,
                            "confidence": float(similarity),
                            "match": True,
                        }
                    }
                else:
                    return {
                        "similarity": {
                            "employee_id": employee_id,
                            "fingerprint_id": target_fingerprint_id,
                            "confidence": float(similarity),
                            "match": False,
                        }
                    }
            else:
                return {
                    "error": f"Target fingerprint ID {target_fingerprint_id} not found in database"
                }

        best_match_fingerprint = None
        best_similarity_fingerprint = -1
        best_fp_employee_id = None

        if fingerprint_embeddings_db:
            for fingerprint_id, fp_data in fingerprint_embeddings_db.items():
                stored_embedding = fp_data["embedding"]
                employee_id = fp_data["employee_id"]

                similarity = np.dot(embedding, stored_embedding) / (
                    np.linalg.norm(embedding) * np.linalg.norm(stored_embedding)
                )

                print(f"Fingerprint ID: {fingerprint_id}, Similarity: {similarity}")

                if similarity > best_similarity_fingerprint:
                    best_similarity_fingerprint = similarity
                    best_match_fingerprint = fingerprint_id
                    best_fp_employee_id = employee_id

            if best_similarity_fingerprint >= threshold:
                return {
                    "similarity": {
                        "employee_id": best_fp_employee_id,
                        "fingerprint_id": best_match_fingerprint,
                        "confidence": float(best_similarity_fingerprint),
                        "match": True,
                    }
                }

            if best_match_fingerprint:
                return {
                    "similarity": {
                        "employee_id": best_fp_employee_id,
                        "fingerprint_id": best_match_fingerprint,
                        "confidence": float(best_similarity_fingerprint),
                        "match": best_similarity_fingerprint >= threshold,
                    }
                }

        best_match_employee = None
        best_similarity_employee = -1

        if employee_embeddings_db:
            for employee_id, stored_embedding in employee_embeddings_db.items():
                similarity = np.dot(embedding, stored_embedding) / (
                    np.linalg.norm(embedding) * np.linalg.norm(stored_embedding)
                )

                if similarity > best_similarity_employee:
                    best_similarity_employee = similarity
                    best_match_employee = employee_id

        matched = best_similarity_employee >= threshold
        result = {
            "similarity": {
                "employee_id": best_match_employee if matched else None,
                "fingerprint_id": None,  
                "confidence": float(best_similarity_employee),
                "match": matched,
            }
        }

        print("Recognition result:", result)
        return result

    except Exception as e:
        print(f"Error during recognition: {e}", file=sys.stderr)
        import traceback

        traceback.print_exc()
        return {"error": str(e)}

@app.route("/api/recognize", methods=["POST"])
def api_recognize():
    """API endpoint to recognize employee from fingerprint image"""
    try:
        if "file" not in request.files:
            return jsonify({"error": "No file part"}), 400

        file = request.files["file"]

        if file.filename == "":
            return jsonify({"error": "No selected file"}), 400

        segmentation_model_path = request.form.get("segmentation_model_path")
        recognition_model_path = request.form.get("recognition_model_path")

        if not segmentation_model_path or not recognition_model_path:
            return jsonify({"error": "Missing model path parameters"}), 400

        filename = secure_filename(file.filename)
        temp_dir = tempfile.mkdtemp()
        filepath = os.path.join(temp_dir, filename)
        file.save(filepath)

        recognition_model, segmentation_model, recognition_shape, segmentation_shape = (
            load_models(
                segmentation_model_path_name=segmentation_model_path,
                recognition_model_path_name=recognition_model_path,
            )
        )

        if recognition_model is None or segmentation_model is None:
            return jsonify({"error": "Failed to load models"}), 500

        embedding_model = create_embedding_model(recognition_model)

        try:

            result = recognize_employee(
                filepath,
                embedding_model,
                segmentation_model,
                recognition_shape,
                segmentation_shape,
            )
            print("result", result)

            serializable_result = convert_to_serializable(result)

            try:
                os.remove(filepath)
                os.rmdir(temp_dir)
            except Exception as e:
                print(f"Warning: Failed to remove temporary file: {e}")

            return jsonify(serializable_result), 200

        except Exception as e:
            print(f"Error during recognition processing: {e}", file=sys.stderr)
            import traceback

            traceback.print_exc()
            return jsonify({"error": f"Recognition failed: {str(e)}"}), 500

    except Exception as e:
        print(f"Error in recognize API: {e}", file=sys.stderr)
        import traceback

        traceback.print_exc()
        return jsonify({"error": str(e)}), 500


@app.route("/api/register", methods=["POST"])
def api_register_fingerprint():
    """API endpoint to register a new fingerprint for an employee"""
    try:
        # Check if the post has the file part
        if "file" not in request.files:
            return jsonify({"error": "No file part"}), 400

        files = request.files.getlist("file")

        # If user does not select file, browser also submits an empty part without filename
        if not files or files[0].filename == "":
            return jsonify({"error": "No selected file"}), 400

        employee_id = request.form.get("employee_id")
        positions = request.form.getlist("position")
        segmentation_model_path = request.form.get("segmentation_model_path")
        recognition_model_path = request.form.get("recognition_model_path")

        # Lấy fingerprint_id là một giá trị đơn
        fingerprint_id = request.form.get("fingerprint_id")
        print("Registering with fingerprint_id:", fingerprint_id)

        if (
            not employee_id
            or not positions
            or not segmentation_model_path
            or not recognition_model_path
        ):
            return jsonify({"error": "Missing required parameters"}), 400

        # Ensure we have position for each file
        if len(positions) != len(files):
            if len(positions) == 1:
                # If only one position is provided, duplicate it for all files
                positions = positions * len(files)
            else:
                return (
                    jsonify(
                        {"error": "Number of positions must match number of files"}
                    ),
                    400,
                )

        # Kiểm tra: chỉ chấp nhận một file nếu có fingerprint_id
        if fingerprint_id and len(files) > 1:
            return (
                jsonify(
                    {"error": "Only one file allowed when fingerprint_id is provided"}
                ),
                400,
            )

        # Save uploaded files temporarily and then process
        temp_dir = tempfile.mkdtemp()
        file_paths = []
        registered_fingerprint_id = None  # Lưu trữ fingerprint ID đã sử dụng

        try:
            script_dir = os.path.dirname(os.path.abspath(__file__))
            dataset_dir = os.path.join(script_dir, "fingerprint_adapting_dataset")
            employee_dir = os.path.join(dataset_dir, employee_id)
            os.makedirs(employee_dir, exist_ok=True)

            # Xóa tất cả các file vân tay cũ liên quan đến fingerprint_id (nếu có)
            if fingerprint_id:
                try:
                    # Tìm và xóa cơ sở dữ liệu cũ
                    embeddings_dbs = load_embeddings_db()
                    fingerprint_embeddings_db = embeddings_dbs.get(
                        "fingerprint_embeddings", {}
                    )

                    if fingerprint_id in fingerprint_embeddings_db:
                        print(
                            f"Removing existing fingerprint ID {fingerprint_id} from database"
                        )
                        del fingerprint_embeddings_db[fingerprint_id]

                        # Lưu lại cơ sở dữ liệu
                        script_dir = os.path.dirname(os.path.abspath(__file__))
                        db_dir = os.path.join(script_dir, "fingerprint_adapting_models")
                        fp_db_path = os.path.join(db_dir, "fingerprint_embeddings.npy")
                        np.save(fp_db_path, fingerprint_embeddings_db)
                except Exception as e:
                    print(f"Error removing existing fingerprint from database: {e}")

            for i, file in enumerate(files):
                # Nếu có fingerprint_id được cung cấp, sử dụng cho file đầu tiên
                # Nếu không, tạo UUID mới
                current_fingerprint_id = None

                if i == 0 and fingerprint_id:
                    current_fingerprint_id = fingerprint_id
                else:
                    current_fingerprint_id = str(uuid.uuid4())

                if i == 0:
                    registered_fingerprint_id = current_fingerprint_id

                # Thêm fingerprint_id vào tên file
                filename = secure_filename(
                    f"{employee_id}_{positions[i]}_{current_fingerprint_id}.bmp"
                )
                filepath = os.path.join(employee_dir, filename)
                file.save(filepath)
                file_paths.append(filepath)

            # Load models
            (
                recognition_model,
                segmentation_model,
                recognition_shape,
                segmentation_shape,
            ) = load_models(
                segmentation_model_path_name=segmentation_model_path,
                recognition_model_path_name=recognition_model_path,
            )

            if recognition_model is None or segmentation_model is None:
                return jsonify({"error": "Failed to load models"}), 500

            # Create embedding model
            embedding_model = create_embedding_model(recognition_model)

            # Xử lý và lưu trữ vân tay trực tiếp thay vì sử dụng update_model
            try:
                # Xử lý vân tay
                processed_img = preprocess_fingerprint(
                    file_paths[0],
                    segmentation_model,
                    recognition_shape,
                    segmentation_shape,
                )
                processed_img = np.expand_dims(processed_img, axis=0)

                # Tạo embedding
                embedding = embedding_model.predict(processed_img, verbose=0)[0]

                # Lưu vào database
                embeddings_dbs = load_embeddings_db()
                fingerprint_embeddings_db = embeddings_dbs.get(
                    "fingerprint_embeddings", {}
                )

                # Lưu theo fingerprint_id
                fingerprint_embeddings_db[registered_fingerprint_id] = {
                    "employee_id": employee_id,
                    "embedding": embedding,
                }

                # Lưu database
                script_dir = os.path.dirname(os.path.abspath(__file__))
                db_dir = os.path.join(script_dir, "fingerprint_adapting_models")
                fp_db_path = os.path.join(db_dir, "fingerprint_embeddings.npy")
                np.save(fp_db_path, fingerprint_embeddings_db)

                # Cập nhật database employee_embeddings (phương pháp cũ)
                employee_embeddings_db = embeddings_dbs.get("employee_embeddings", {})

                if employee_id in employee_embeddings_db:
                    # Tính trung bình với embedding cũ
                    old_emb = employee_embeddings_db[employee_id]
                    employee_embeddings_db[employee_id] = (old_emb + embedding) / 2
                else:
                    employee_embeddings_db[employee_id] = embedding

                # Lưu database employee
                db_path = os.path.join(db_dir, "employee_embeddings.npy")
                np.save(db_path, employee_embeddings_db)

                print(
                    f"Successfully saved fingerprint with ID {registered_fingerprint_id}"
                )

                # Kiểm tra database sau khi lưu
                check_db = load_embeddings_db()
                check_fp_db = check_db.get("fingerprint_embeddings", {})
                if registered_fingerprint_id in check_fp_db:
                    print(
                        f"Verified: Fingerprint ID {registered_fingerprint_id} exists in database"
                    )
                else:
                    print(
                        f"WARNING: Fingerprint ID {registered_fingerprint_id} NOT found in database after saving"
                    )

                success = True
            except Exception as e:
                print(f"Error processing fingerprint: {e}", file=sys.stderr)
                import traceback

                traceback.print_exc()
                success = False

            if success:
                return (
                    jsonify(
                        {
                            "message": "Fingerprint registered successfully",
                            "employeeId": employee_id,
                            "fingerprint_id": registered_fingerprint_id,  # Trả về fingerprint ID đã đăng ký
                        }
                    ),
                    200,
                )
            else:
                return (
                    jsonify(
                        {
                            "status": "error",
                            "message": "Failed to process fingerprint after registration",
                        }
                    ),
                    500,
                )

        except Exception as e:
            print(f"Error in fingerprint registration: {e}", file=sys.stderr)
            import traceback

            traceback.print_exc()
            return jsonify({"error": str(e)}), 500
        finally:
            try:
                # Don't remove the files in the dataset directory
                os.rmdir(temp_dir)
            except:
                pass

    except Exception as e:
        print(f"Error in API: {e}", file=sys.stderr)
        import traceback

        traceback.print_exc()
        return jsonify({"error": str(e)}), 500


@app.route("/health", methods=["GET"])
def health_check():
    """Health check endpoint"""
    return jsonify({"status": "ok", "message": "Fingerprint API is running"}), 200


if __name__ == "__main__":
    # Khởi động Flask server
    app.run(host="0.0.0.0", port=5000, debug=False)
