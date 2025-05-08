
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
            raise ValueError(f"Cannot read image at {image_path}")

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
        print(f"Error in preprocess_fingerprint for {image_path}: {e}", file=sys.stderr)
        raise

def create_embedding_model(recognition_model):
    feature_extractor = recognition_model.get_layer('functional')
    input_shape = feature_extractor.input_shape[1:]
    new_input = keras.layers.Input(shape=input_shape)
    embedding = feature_extractor(new_input)
    embedding_model = keras.Model(inputs=new_input, outputs=embedding)
    return embedding_model

