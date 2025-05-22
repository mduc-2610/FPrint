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
        segmentation_model_path_name="unet_segmentation_v1_0",
        recognition_model_path_name="siamese_network_v1_0", 
        ):
    try:
        script_dir = os.path.dirname(os.path.abspath(__file__))
        recognition_model_path = os.path.join(script_dir, f'fingerprint_models/recognition/{recognition_model_path_name}.keras')
        segmentation_model_path = os.path.join(script_dir, f'fingerprint_models/segmentation/{segmentation_model_path_name}.keras')
        
        print(f"Loading recognition model from {recognition_model_path}")
        print(f"Loading segmentation model from {segmentation_model_path}")

        recognition_model = load_model(recognition_model_path, custom_objects={'IoU': IoU})
        segmentation_model = load_model(segmentation_model_path, custom_objects={'IoU': IoU})

        if isinstance(recognition_model.input, list):
            recognition_shape = recognition_model.input[0].shape[1:3]
        else:
            recognition_shape = recognition_model.input_shape[1:3]

        if isinstance(segmentation_model.input, list):
            segmentation_shape = segmentation_model.input[0].shape[1:3]
        else:
            segmentation_shape = segmentation_model.input_shape[1:3]
        
        print(f"Recognition model input shape: {recognition_shape}")
        print(f"Segmentation model input shape: {segmentation_shape}")

        return recognition_model, segmentation_model, recognition_shape, segmentation_shape

    except Exception as e:
        print(f"Error loading models: {e}", file=sys.stderr)
        return None, None, None, None

def preprocess_fingerprint(image_path, segmentation_model, recognition_shape, segmentation_shape):
    try:
        img = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)
        if img is None:
            raise ValueError(f"Could not load image from {image_path}")

        img_for_segmentation = cv2.resize(img, (segmentation_shape[1], segmentation_shape[0]))

        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
        img_for_segmentation = clahe.apply(img_for_segmentation)

        img_for_segmentation = np.expand_dims(np.expand_dims(img_for_segmentation, axis=-1), axis=0) / 255.0

        segmentation_output = segmentation_model.predict(img_for_segmentation, verbose=0)

        if isinstance(segmentation_output, list) and len(segmentation_output) > 0:
            mask = segmentation_output[0][0]
        else:
            mask = segmentation_output[0]

        mask = (mask > 0.5).astype(np.uint8)

        if len(mask.shape) == 3:
            mask_2d = mask[:, :, 0]
        else:
            mask_2d = mask

        mask_resized = cv2.resize(mask_2d, (img.shape[1], img.shape[0]))
        img = img * mask_resized

        img = cv2.resize(img, (recognition_shape[1], recognition_shape[0]))

        img = img / 255.0
        img = np.expand_dims(img, axis=-1)

        return img

    except Exception as e:
        print(f"Error in preprocess_fingerprint: {e}", file=sys.stderr)
        raise

def create_embedding_model(recognition_model):
    try:
        feature_extractor = recognition_model.get_layer('functional')
        input_shape = feature_extractor.input_shape[1:]
        new_input = keras.layers.Input(shape=input_shape)
        embedding = feature_extractor(new_input)
        embedding_model = keras.Model(inputs=new_input, outputs=embedding)
        print("Embedding model created successfully")
        return embedding_model
    except Exception as e:
        print(f"Error creating embedding model: {e}", file=sys.stderr)
        raise

def update_fingerprint_model(segmentation_model_path_name="unet_segmentation_v1_0", 
                            recognition_model_path_name="siamese_network_v1_0"):
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
    dataset_dir = os.path.join(script_dir, "employee_fingerprint")
    
    if not os.path.exists(dataset_dir):
        print(f"Dataset directory not found: {dataset_dir}", file=sys.stderr)
        print("Please create the directory and add fingerprint images.")
        return False
    
    print(f"Scanning dataset directory: {dataset_dir}")
    
    db_dir = os.path.join(script_dir, "embedding_models")
    os.makedirs(db_dir, exist_ok=True)
    db_path = os.path.join(db_dir, "employee_embeddings.npy")
    
    if os.path.exists(db_path):
        try:
            print(f"Loading existing embeddings database from {db_path}")
            embeddings_db = np.load(db_path, allow_pickle=True).item()
            print(f"Found {len(embeddings_db)} existing employee embeddings")
        except Exception as e:
            print(f"Error loading existing database: {e}. Creating new database.", file=sys.stderr)
            embeddings_db = {}
    else:
        print("No existing embeddings database found. Creating new database.")
        embeddings_db = {}
    
    total_employees = 0
    total_fingerprints = 0
    failed_fingerprints = 0
    updated_employees = 0
    
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
                    
                    norm = np.linalg.norm(embedding)
                    if norm > 0: 
                        embedding = embedding / norm
                    
                    employee_embeddings.append(embedding)
                    print(f"  Processed {os.path.basename(image_path)}")
                    total_fingerprints += 1
                except Exception as e:
                    print(f"  Error processing {image_path}: {e}", file=sys.stderr)
                    failed_fingerprints += 1
            
            if employee_embeddings:
                avg_embedding = np.mean(employee_embeddings, axis=0)
                
                norm = np.linalg.norm(avg_embedding)
                if norm > 0: 
                    avg_embedding = avg_embedding / norm
                
                if employee_id in embeddings_db:
                    print(f"  Updating existing employee {employee_id}")
                    updated_employees += 1
                else:
                    print(f"  Adding new employee {employee_id}")
                    total_employees += 1
                
                embeddings_db[employee_id] = avg_embedding
                print(f"  Added employee {employee_id} with {len(employee_embeddings)} fingerprints")
                
                print(f"  Sample values: {avg_embedding[:5]}")
                
                if np.all(np.abs(avg_embedding) < 1e-5):
                    print(f"  WARNING: Embedding for employee {employee_id} appears to be all zeros or near-zero!", file=sys.stderr)
            else:
                print(f"  No valid fingerprints processed for employee {employee_id}")
    
    if embeddings_db:
        np.save(db_path, embeddings_db)
        
        print("\nUpdate Summary:")
        print(f"- Total employees in database: {len(embeddings_db)}")
        print(f"- New employees added: {total_employees}")
        print(f"- Existing employees updated: {updated_employees}")
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