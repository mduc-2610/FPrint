
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
from common import IoU, load_models, preprocess_fingerprint, create_embedding_model

def update_fingerprint_model(segmentation_model_path_name, recognition_model_path_name):
    print("Starting fingerprint model update...")
    
    recognition_model, segmentation_model, recognition_shape, segmentation_shape = load_models(
        segmentation_model_path_name=segmentation_model_path_name,
        recognition_model_path_name=recognition_model_path_name
    )
    
    if recognition_model is None or segmentation_model is None:
        print("Failed to load models. Please check model paths and try again.", file=sys.stderr)
        return False

    print("Creating embedding model...")
    embedding_model = create_embedding_model(recognition_model)

    script_dir = os.path.dirname(os.path.abspath(__file__))
    dataset_dir = os.path.join(script_dir, "fingerprint_adapting_dataset")
    
    if not os.path.exists(dataset_dir):
        print(f"Dataset directory not found: {dataset_dir}", file=sys.stderr)
        print("Please create the directory and add fingerprint images.")
        return False
    
    print(f"Scanning dataset directory: {dataset_dir}")
    
    embeddings_db = {}
    
    total_employees = 0
    total_fingerprints = 0
    failed_fingerprints = 0
    
    for employee_id in os.listdir(dataset_dir):
        employee_folder = os.path.join(dataset_dir, employee_id)
        if os.path.isdir(employee_folder):
            print(f"Processing employee {employee_id}...")
            
            fingerprint_images = []
            for file_name in os.listdir(employee_folder):
                if file_name.lower().endswith(('.bmp', '.png', '.jpg', '.jpeg', '.tif', '.tiff')):
                    fingerprint_images.append(os.path.join(employee_folder, file_name))
            
            if not fingerprint_images:
                print(f"No fingerprint images found for employee {employee_id}")
                continue
                
            employee_embeddings = []
            for image_path in fingerprint_images:
                try:
                    processed_img = preprocess_fingerprint(
                        image_path, segmentation_model, recognition_shape, segmentation_shape
                    )
                    processed_img = np.expand_dims(processed_img, axis=0)
                    
                    embedding = embedding_model.predict(processed_img, verbose=0)[0]
                    
                    # Normalize the embedding
                    embedding = embedding / np.linalg.norm(embedding)
                    
                    employee_embeddings.append(embedding)
                    print(f"  Processed {os.path.basename(image_path)}")
                    total_fingerprints += 1
                except Exception as e:
                    print(f"  Error processing {image_path}: {e}", file=sys.stderr)
                    failed_fingerprints += 1
            
            if employee_embeddings:
                # Take the average of all embeddings for this employee
                avg_embedding = np.mean(employee_embeddings, axis=0)
                
                # Normalize the final embedding again
                avg_embedding = avg_embedding / np.linalg.norm(avg_embedding)
                
                embeddings_db[employee_id] = avg_embedding
                total_employees += 1
                print(f"  Added employee {employee_id} with {len(employee_embeddings)} fingerprints")
                
                # Debug: Print a sample of the embedding
                print(f"  Sample values: {avg_embedding[:5]}")
            else:
                print(f"  No valid fingerprints processed for employee {employee_id}")
    
    if embeddings_db:
        db_dir = os.path.join(script_dir, "fingerprint_adapting_models")
        os.makedirs(db_dir, exist_ok=True)
        
        db_path = os.path.join(db_dir, "employee_embeddings.npy")
        
        # Save the updated database
        np.save(db_path, embeddings_db)
        
        print("\nUpdate Summary:")
        print(f"- Processed {total_employees} employees")
        print(f"- Processed {total_fingerprints} fingerprints successfully")
        if failed_fingerprints > 0:
            print(f"- Failed to process {failed_fingerprints} fingerprints")
        print(f"- Embeddings database saved to {db_path}")
        
        return True
    else:
        print("\nNo valid employee data found. Database not updated.", file=sys.stderr)
        return False

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Update fingerprint recognition model from dataset")
    parser.add_argument("--seg-model", default="unet_segmentation_v1_0", help="Segmentation model name")
    parser.add_argument("--rec-model", default="siamese_network_v1_0", help="Recognition model name")
    
    args = parser.parse_args()
    
    success = update_fingerprint_model(
        segmentation_model_path_name=args.seg_model,
        recognition_model_path_name=args.rec_model
    )
    
    if success:
        print("\nFingerprint recognition model updated successfully!")
        sys.exit(0)
    else:
        print("\nFailed to update fingerprint recognition model.", file=sys.stderr)
        sys.exit(1)