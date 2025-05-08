import numpy as np
import tensorflow as tf
import keras
import os
import cv2
import sys
import json
import tempfile
import uuid  
from flask import Flask, request, jsonify
from tensorflow.keras.models import load_model
from tensorflow.keras.metrics import Metric
from werkzeug.utils import secure_filename
from common import IoU, load_models, preprocess_fingerprint, create_embedding_model

app = Flask(__name__)

DATASET_DIR = "fingerprint_adapting_dataset"
os.makedirs(DATASET_DIR, exist_ok=True)
UPLOAD_FOLDER = "temp_uploads"
MODELS_DIR = os.path.dirname(os.path.abspath(__file__))
os.makedirs(UPLOAD_FOLDER, exist_ok=True)


def convert_to_serializable(obj):
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

@app.route("/api/create-id-folders/", methods=["POST"])
def create_id_folders():
    # Get data from request
    data = request.get_json()
    ids = data.get("ids", [])
    reset = data.get("reset", False)
    
    print(f"Received IDs: {ids}")
    print(f"Reset flag: {reset}")
    os.makedirs(DATASET_DIR, exist_ok=True)
    
    if reset:
        for item in os.listdir(DATASET_DIR):
            item_path = os.path.join(DATASET_DIR, item)
            if os.path.isdir(item_path):
                shutil.rmtree(item_path)
    
    created_folders = []
    for id in ids:
        folder_path = os.path.join(DATASET_DIR, id)
        
        if not os.path.exists(folder_path):
            os.makedirs(folder_path)
            created_folders.append(id)
    
    return jsonify({
        "message": f"Created {len(created_folders)} folders",
        "created_folders": created_folders,
        "total_folders": len(ids),
        "reset_performed": reset
    })



def load_embeddings_db():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    db_dir = os.path.join(script_dir, "fingerprint_adapting_models")
    db_path = os.path.join(db_dir, "employee_embeddings.npy")
    
    # Initialize empty result
    result = {"employee_embeddings": {}}
    
    # Check if the main database file exists
    if os.path.exists(db_path):
        try:
            # Load the main database file
            embeddings_db = np.load(db_path, allow_pickle=True).item()
            result["employee_embeddings"] = embeddings_db
            print(f"Loaded {len(embeddings_db)} employee embeddings from {db_path}")
            
            # Debug: Print the actual embeddings loaded
            for emp_id, embedding in embeddings_db.items():
                print(f"Employee {emp_id} embedding shape: {embedding.shape}, type: {type(embedding)}")
                # Print a sample of the embedding to ensure it's not all zeros or invalid
                print(f"Sample values: {embedding[:5]}")
        except Exception as e:
            print(f"Error loading employee embeddings database: {e}", file=sys.stderr)
            result["employee_embeddings"] = {}
    else:
        print(f"Employee embeddings database not found at {db_path}", file=sys.stderr)
    
    return result


"""
Next, let's fix the recognize_employee function:
1. Use a more reasonable threshold (0.7 is common for cosine similarity)
2. Better debug output
3. Fix the comparison logic
"""

def recognize_employee(
    image_path,
    embedding_model,
    segmentation_model,
    recognition_shape,
    segmentation_shape,
    threshold=0.7,  # Changed from 1.0 to a more reasonable value
    target_employee_id=None,
):
    try:
        # Load embeddings database
        embeddings_dbs = load_embeddings_db()
        employee_embeddings_db = embeddings_dbs.get("employee_embeddings", {})

        if not employee_embeddings_db:
            print("ERROR: Embeddings database is empty!")
            return {"error": "Embeddings database is empty"}

        print("Available employee IDs:", list(employee_embeddings_db.keys()))

        # Process the input fingerprint image
        print(f"Processing image: {image_path}")
        processed_img = preprocess_fingerprint(
            image_path, segmentation_model, recognition_shape, segmentation_shape
        )
        processed_img = np.expand_dims(processed_img, axis=0)

        # Generate embedding for the input image
        embedding = embedding_model.predict(processed_img, verbose=0)[0]
        print(f"Generated embedding shape: {embedding.shape}")
        print(f"Sample values: {embedding[:5]}")
        
        # Normalize the input embedding (important for cosine similarity)
        embedding = embedding / np.linalg.norm(embedding)

        # If a specific employee is targeted
        if target_employee_id and target_employee_id in employee_embeddings_db:
            stored_embedding = employee_embeddings_db[target_employee_id]
            
            # Normalize the stored embedding
            stored_embedding = stored_embedding / np.linalg.norm(stored_embedding)
            
            # Calculate cosine similarity
            similarity = np.dot(embedding, stored_embedding)
            print(f"Target employee: {target_employee_id}, Similarity: {similarity}")

            return {
                "similarity": {
                    "employee_id": target_employee_id,
                    "confidence": float(similarity),
                    "match": similarity >= threshold,
                }
            }
        elif target_employee_id:
            return {
                "error": f"Target employee ID {target_employee_id} not found in database"
            }

        # Find best matching employee
        best_match_employee = None
        best_similarity_employee = -1

        # Debug: Print all similarity scores
        print("Calculating similarity with all employees:")
        
        for employee_id, stored_embedding in employee_embeddings_db.items():
            # Normalize the stored embedding
            stored_embedding = stored_embedding / np.linalg.norm(stored_embedding)
            
            # Calculate cosine similarity
            similarity = np.dot(embedding, stored_embedding)
            
            print(f"Employee ID: {employee_id}, Similarity: {similarity}")

            if similarity > best_similarity_employee:
                best_similarity_employee = similarity
                best_match_employee = employee_id

        matched = best_similarity_employee >= threshold
        result = {
            "similarity": {
                "employee_id": best_match_employee if matched else None,
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


def recognize_employee(
    image_path,
    embedding_model,
    segmentation_model,
    recognition_shape,
    segmentation_shape,
    threshold=1.0,
    target_employee_id=None,
):
    try:
        embeddings_dbs = load_embeddings_db()
        employee_embeddings_db = embeddings_dbs.get("employee_embeddings", {})

        if not employee_embeddings_db:
            return {"error": "Embeddings database is empty"}

        print("Available employee IDs:", list(employee_embeddings_db.keys()))

        if target_employee_id:
            print(f"Target employee ID: {target_employee_id}")
            print(
                f"Is target in database: {target_employee_id in employee_embeddings_db}"
            )

        processed_img = preprocess_fingerprint(
            image_path, segmentation_model, recognition_shape, segmentation_shape
        )
        processed_img = np.expand_dims(processed_img, axis=0)

        embedding = embedding_model.predict(processed_img, verbose=0)[0]

        # If a specific employee is targeted
        if target_employee_id and target_employee_id in employee_embeddings_db:
            stored_embedding = employee_embeddings_db[target_employee_id]
            
            similarity = np.dot(embedding, stored_embedding) / (
                np.linalg.norm(embedding) * np.linalg.norm(stored_embedding)
            )

            return {
                "similarity": {
                    "employee_id": target_employee_id,
                    "confidence": float(similarity),
                    "match": similarity >= threshold,
                }
            }
        elif target_employee_id:
            return {
                "error": f"Target employee ID {target_employee_id} not found in database"
            }

        # Find best matching employee
        best_match_employee = None
        best_similarity_employee = -1

        for employee_id, stored_embedding in employee_embeddings_db.items():
            similarity = np.dot(embedding, stored_embedding) / (
                np.linalg.norm(embedding) * np.linalg.norm(stored_embedding)
            )
            
            print(f"Employee ID: {employee_id}, Similarity: {similarity}")

            if similarity > best_similarity_employee:
                best_similarity_employee = similarity
                best_match_employee = employee_id

        matched = best_similarity_employee >= threshold
        result = {
            "similarity": {
                "employee_id": best_match_employee if matched else None,
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
def api_recognize_fingerprint():
    try:
        if "file" not in request.files:
            return jsonify({"error": "No file part"}), 400

        file = request.files["file"]

        if file.filename == "":
            return jsonify({"error": "No selected file"}), 400

        segmentation_model_path = request.form.get("segmentation_model_path")
        recognition_model_path = request.form.get("recognition_model_path")
        target_employee_id = request.form.get("employee_id")  

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
                target_employee_id=target_employee_id,
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
    

@app.route("/api/models", methods=["GET"])
def get_models():
    try:
        script_dir = os.path.dirname(os.path.abspath(__file__))
        
        segmentation_models_path = os.path.join(script_dir, "reports", "fingerprint_segmentation_models.json")
        recognition_models_path = os.path.join(script_dir, "reports", "fingerprint_recognition_models.json")
        
        segmentation_models = []
        recognition_models = []
        
        if os.path.exists(segmentation_models_path):
            try:
                with open(segmentation_models_path, 'r') as file:
                    segmentation_models = json.load(file)
            except Exception as e:
                print(f"Error loading segmentation models data: {e}", file=sys.stderr)
        
        if os.path.exists(recognition_models_path):
            try:
                with open(recognition_models_path, 'r') as file:
                    recognition_models = json.load(file)
            except Exception as e:
                print(f"Error loading recognition models data: {e}", file=sys.stderr)
        
        return jsonify({
            "segmentation_models": segmentation_models,
            "recognition_models": recognition_models
        }), 200
        
    except Exception as e:
        print(f"Error in get_models API: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500
    
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=False)
