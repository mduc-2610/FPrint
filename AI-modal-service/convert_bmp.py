import os
import sys
from PIL import Image

def convert_images(directory, source_ext=None, target_ext="BMP"):
    if not os.path.isdir(directory):
        print(f"Error: {directory} is not a valid directory.")
        return

    for root, _, files in os.walk(directory):
        for file in files:
            file_path = os.path.join(root, file)
            try:
                file_ext = file.split(".")[-1].lower()
                if source_ext:
                    if file_ext != source_ext.lower():
                        continue  # Skip files that don't match source extension
                
                with Image.open(file_path) as img:
                    if img.format == target_ext.upper():
                        continue  # Skip files that are already in target format
                    
                    target_path = os.path.splitext(file_path)[0] + f".{target_ext.lower()}"
                    img.convert("RGB").save(target_path, target_ext.upper())
                    print(f"Converted: {file_path} -> {target_path}")
                
                img.close()  # Ensure the image is fully closed before deletion
                os.remove(file_path)  # Delete the original file
                print(f"Deleted: {file_path}")
            except Exception as e:
                print(f"Skipping {file_path}: {e}")

if __name__ == "__main__":
    args = sys.argv
    if len(args) < 2 or len(args) > 3:
        print("Usage: python script.py <directory> [source_ext target_ext]")
        sys.exit(1)
    
    dir_path = args[1]
    source_ext = args[2] if len(args) > 2 else None
    target_ext = args[3] if len(args) > 3 else "BMP"
    
    convert_images(dir_path, source_ext, target_ext)


# fingerprint_training/ │ ├── fingerprint_adapting_dataset/ (employee fingerprint registered store here) │ │ ├── 101/ (id then fingerprint image file inside tif or bmp, ) │ │ ├── 102/ │ │ └── ... │ │ fingerprint_models │ │ ├── recognition │ │ ├──------siamese_network.keras │ │ ├──segmentation │ │ ├──── unet_segmentation.keras │ │ fingerprint_recognition │ │ ├── model_with_segmentation.ipynb │ │ ├── dataset │ │ ├──── x_easy.npy │ │ ├──── x_hard.npy │ │ 1. U-Net Segmentation Model Architecture Your U-Net model is designed for fingerprint segmentation - separating the fingerprint from the background in images. Architecture Details: Input: 90×90×1 grayscale fingerprint images Encoder (Contracting) Path: First block: Two Conv2D layers (16 filters) → MaxPooling (reduces to 45×45) Second block: Two Conv2D layers (32 filters) → MaxPooling (reduces to 22×22) Third block: Two Conv2D layers (64 filters) → MaxPooling (reduces to 11×11) Bottleneck: Two Conv2D layers (128 filters) Decoder (Expanding) Path: First upsampling: Conv2DTranspose (64 filters) → Concatenate with encoder features Two Conv2D layers (64 filters) Second upsampling: Conv2DTranspose (32 filters) → Concatenate with encoder features Two Conv2D layers (32 filters) Third upsampling: Conv2DTranspose (16 filters) → Concatenate with encoder features Two Conv2D layers (16 filters) Final Conv2D layer (1 filter) for output mask ZeroPadding to restore original 90×90 dimensions Skip Connections: Connect corresponding layers between encoder and decoder Total Parameters: ~480K parameters Purpose: The segmentation model isolates the fingerprint from background noise, which dramatically improves recognition accuracy. The clean, segmented fingerprints are then fed to the recognition model. 2. Siamese Network Recognition Model Your Siamese network determines if two fingerprints belong to the same person. Architecture Details: Dual Inputs: Two 90×90×1 grayscale fingerprint images Shared Feature Extraction: Both inputs go through identical convolutional layers with shared weights Feature Processing: Output of shared network is a 30,976-dimensional feature vector for each input Subtract feature vectors to compute a difference vector Decision Layers: Dense layer (128 neurons) processes the difference vector Dropout layer (reduces overfitting) Final Dense layer (1 neuron) with sigmoid activation outputs a similarity score Total Parameters: ~4M parameters

# i have two models siamese and unet for fingerprint recognition and have trained how can i update the model each time employee is registered a new fingerprint then employee i fingerprint if valid can be recognized
# your generated code should be reusable because i want to recognize to java backend spring with two moduls
# register employee's fingerprint(upload fngerprint image file then validate)
# recognize employee's fingerprint (upload fingerprint image then use model which have reinforced with new fingerprint image register from employee to recognize)