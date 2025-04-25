#!/usr/bin/env python
# coding: utf-8

import numpy as np
import tensorflow as tf
import keras
import os
import cv2
import sys
import argparse
import json
from tensorflow.keras.models import load_model
from tensorflow.keras.metrics import Metric

@keras.saving.register_keras_serializable()
class IoU(Metric):
    def __init__(self, name='iou', **kwargs):
        super(IoU, self).__init__(name=name, **kwargs)
        self.intersection = self.add_weight(name='intersection', initializer='zeros')
        self.union = self.add_weight(name='union', initializer='zeros')

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

def load_models(
        segmentation_model_path_name,
        recognition_model_path_name, 
        ):
    """Load the fingerprint recognition and segmentation models"""
    try:
        # Define model paths relative to script location
        script_dir = os.path.dirname(os.path.abspath(__file__))
        recognition_model_path = os.path.join(script_dir, f'fingerprint_models/recognition/{recognition_model_path_name}.keras')
        segmentation_model_path = os.path.join(script_dir, f'fingerprint_models/segmentation/{segmentation_model_path_name}.keras')

        # Load models
        recognition_model = load_model(recognition_model_path, custom_objects={'IoU': IoU})
        segmentation_model = load_model(segmentation_model_path, custom_objects={'IoU': IoU})

        # Get input shapes
        if isinstance(recognition_model.input, list):
            recognition_shape = recognition_model.input[0].shape[1:3]
        else:
            recognition_shape = recognition_model.input_shape[1:3]

        if isinstance(segmentation_model.input, list):
            segmentation_shape = segmentation_model.input[0].shape[1:3]
        else:
            segmentation_shape = segmentation_model.input_shape[1:3]

        return recognition_model, segmentation_model, recognition_shape, segmentation_shape

    except Exception as e:
        print(f"Error loading models: {e}", file=sys.stderr)
        return None, None, (90, 90), (90, 90)

def preprocess_fingerprint(image_path, segmentation_model, recognition_shape, segmentation_shape):
    """Preprocess fingerprint images to match model requirements"""
    try:
        # Read image
        img = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)
        if img is None:
            raise ValueError(f"Could not load image from {image_path}")

        # Resize for segmentation
        img_for_segmentation = cv2.resize(img, (segmentation_shape[1], segmentation_shape[0]))

        # Enhance contrast
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
        img_for_segmentation = clahe.apply(img_for_segmentation)

        # Prepare for segmentation model
        img_for_segmentation = np.expand_dims(np.expand_dims(img_for_segmentation, axis=-1), axis=0) / 255.0

        # Get segmentation mask
        segmentation_output = segmentation_model.predict(img_for_segmentation, verbose=0)

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
    feature_extractor = recognition_model.get_layer('functional')
    input_shape = feature_extractor.input_shape[1:]
    new_input = keras.layers.Input(shape=input_shape)
    embedding = feature_extractor(new_input)
    embedding_model = keras.Model(inputs=new_input, outputs=embedding)
    return embedding_model

def process_new_employee(employee_id, fingerprint_images_paths, segmentation_model, recognition_shape, segmentation_shape):
    """Process new employee fingerprints"""
    print(f"Processing fingerprints for employee {employee_id}")

    script_dir = os.path.dirname(os.path.abspath(__file__))
    processed_dir = os.path.join(script_dir, f"processed_fingerprints/employee_{employee_id}")
    os.makedirs(processed_dir, exist_ok=True)

    fingerprints = []

    for i, img_path in enumerate(fingerprint_images_paths):
        try:
            processed_img = preprocess_fingerprint(img_path, segmentation_model, recognition_shape, segmentation_shape)
            processed_path = os.path.join(processed_dir, f"fp_{i}.npy")
            np.save(processed_path, processed_img)
            fingerprints.append(processed_img)
        except Exception as e:
            print(f"  Error processing {img_path}: {e}", file=sys.stderr)

    if len(fingerprints) == 0:
        raise ValueError(f"No fingerprints were successfully processed for employee {employee_id}")

    return np.array(fingerprints)

def generate_employee_data(dir_path):
    """Generate employee data from directory structure"""
    employee_data = {}
    for employee_id in os.listdir(dir_path):
        employee_folder = os.path.join(dir_path, employee_id)
        if os.path.isdir(employee_folder):
            employee_data[employee_id] = []
            for fingerprint_file in os.listdir(employee_folder):
                employee_data[employee_id].append(os.path.join(employee_folder, fingerprint_file))
    return employee_data

def enroll_employees(employee_data_paths, embedding_model, segmentation_model, recognition_shape, segmentation_shape):
    """Enroll employees and create embeddings database"""
    new_employees_data = {}

    for employee_id, image_paths in employee_data_paths.items():
        try:
            fingerprints = process_new_employee(employee_id, image_paths, segmentation_model, recognition_shape, segmentation_shape)
            new_employees_data[employee_id] = fingerprints
            print(f"Successfully enrolled employee {employee_id} with {len(fingerprints)} fingerprints")
        except Exception as e:
            print(f"Failed to enroll employee {employee_id}: {e}", file=sys.stderr)

    if not new_employees_data:
        raise ValueError("No employees were successfully enrolled")

    # Create embeddings database
    embeddings_db = {}
    for employee_id, fingerprints in new_employees_data.items():
        employee_embeddings = embedding_model.predict(fingerprints, verbose=0)
        embeddings_db[employee_id] = np.mean(employee_embeddings, axis=0)  # Store average embedding

    # Save embeddings database
    script_dir = os.path.dirname(os.path.abspath(__file__))
    db_dir = os.path.join(script_dir, "fingerprint_adapting_models")
    os.makedirs(db_dir, exist_ok=True)

    db_path = os.path.join(db_dir, "employee_embeddings.npy")
    np.save(db_path, embeddings_db)

    return embeddings_db

def load_embeddings_db():
    """Load embeddings database from file"""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    db_path = os.path.join(script_dir, "fingerprint_adapting_models/employee_embeddings.npy")

    if os.path.exists(db_path):
        embeddings_db = np.load(db_path, allow_pickle=True).item()
        return embeddings_db
    else:
        print(f"Embeddings database not found at {db_path}", file=sys.stderr)
        return None

def recognize_employee(image_path, embedding_model, segmentation_model, recognition_shape, segmentation_shape, threshold=1.0):
    """Recognize employee from fingerprint"""
    try:
        # Load embeddings database
        embeddings_db = load_embeddings_db()
        if embeddings_db is None:
            return {"error": "Embeddings database not found"}

        # Preprocess image
        processed_img = preprocess_fingerprint(image_path, segmentation_model, recognition_shape, segmentation_shape)
        processed_img = np.expand_dims(processed_img, axis=0)

        # Get embedding for the input fingerprint
        embedding = embedding_model.predict(processed_img, verbose=0)[0]

        # Find most similar embedding
        best_match = None
        best_similarity = -1

        for employee_id, stored_embedding in embeddings_db.items():
            # Compute cosine similarity
            similarity = np.dot(embedding, stored_embedding) / (np.linalg.norm(embedding) * np.linalg.norm(stored_embedding))

            if similarity > best_similarity:
                best_similarity = similarity
                best_match = employee_id

        # Prepare result
        if best_similarity >= threshold:
            result = {"similarity": {"employee_id": best_match, "confidence": float(best_similarity)}}
        else:
            result = {"similarity": {"employee_id": None, "confidence": float(best_similarity)}}
            
        # Save result to JSON file
        script_dir = os.path.dirname(os.path.abspath(__file__))
        result_path = os.path.join(script_dir, "reports", "recognition_result.json")
        
        with open(result_path, 'w') as f:
            json.dump(result, f)
            
        print(f"Recognition result saved to {result_path}")
        
        return result

    except Exception as e:
        print(f"Error during recognition: {e}", file=sys.stderr)
        error_result = {"error": str(e)}
        
        # Save error result to JSON file
        script_dir = os.path.dirname(os.path.abspath(__file__))
        result_path = os.path.join(script_dir, "reports", "recognition_result.json")
        
        with open(result_path, 'w') as f:
            json.dump(error_result, f)
            
        return error_result

def update_model(
    segmentation_model_path_name,
    recognition_model_path_name,
):
    """Update fingerprint recognition model with new data"""
    recognition_model, segmentation_model, recognition_shape, segmentation_shape = load_models(
        segmentation_model_path_name=segmentation_model_path_name,
        recognition_model_path_name=recognition_model_path_name, 
    )
    if recognition_model is None or segmentation_model is None:
        print("Failed to load models. Please check model paths and formats.", file=sys.stderr)
        return False

    # Create embedding model
    embedding_model = create_embedding_model(recognition_model)

    # Get employee data
    script_dir = os.path.dirname(os.path.abspath(__file__))
    dataset_dir = os.path.join(script_dir, "fingerprint_adapting_dataset")
    employee_data_paths = generate_employee_data(dataset_dir)

    # Enroll employees
    try:
        embeddings_db = enroll_employees(employee_data_paths, embedding_model, segmentation_model, recognition_shape, segmentation_shape)
        return True
    except Exception as e:
        print(f"An error occurred during model update: {e}", file=sys.stderr)
        return False

def main():
    """Main function to handle command line arguments"""
    parser = argparse.ArgumentParser(description='Fingerprint Recognition System')

    # Command line arguments
    parser.add_argument('--update-model', action='store_true', help='Update fingerprint model with new data')
    parser.add_argument('--recognize', type=str, help='Recognize employee from fingerprint image')
    parser.add_argument('--seg-path-name', type=str, help='Segmentation model path name')
    parser.add_argument('--rec-path-name', type=str, help='Recognition model path name')

    args = parser.parse_args()

    # Load models
    recognition_model, segmentation_model, recognition_shape, segmentation_shape = load_models(
        segmentation_model_path_name=args.seg_path_name,
        recognition_model_path_name=args.rec_path_name, 
    )
    if recognition_model is None or segmentation_model is None:
        print("Failed to load models. Please check model paths and formats.", file=sys.stderr)
        sys.exit(1)

    # Create embedding model
    embedding_model = create_embedding_model(recognition_model)

    # Handle update model command
    if args.update_model:
        success = update_model(
            recognition_model_path_name=args.rec_path_name, 
            segmentation_model_path_name=args.seg_path_name
        )
        if success:
            print("Model updated successfully")
            sys.exit(0)
        else:
            print("Failed to update model", file=sys.stderr)
            sys.exit(1)

    # Handle recognize command
    if args.recognize:
        if not os.path.exists(args.recognize):
            print(f"Image file not found: {args.recognize}", file=sys.stderr)
            sys.exit(1)

        result = recognize_employee(args.recognize, embedding_model, segmentation_model, recognition_shape, segmentation_shape)
        print(json.dumps(result))
        sys.exit(0)

    # If no arguments provided, show help
    parser.print_help()
    sys.exit(1)

if __name__ == '__main__':
    main()