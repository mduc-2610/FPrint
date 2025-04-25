import os
import shutil
import re

def refactor_fingerprint_folders(source_dir, target_dir):
    """
    Refactors fingerprint files from a flat structure into a hierarchical one
    organized by employee ID.
    
    Args:
        source_dir (str): Directory containing the fingerprint TIFF files
        target_dir (str): Directory where the reorganized structure will be created
    
    Returns:
        dict: A nested dictionary with employee IDs and their fingerprints
    """
    # Create target directory if it doesn't exist
    if not os.path.exists(target_dir):
        os.makedirs(target_dir)
    
    # Dictionary to store the data structure
    employee_data = {}
    
    # Regular expression to match fingerprint files
    # Format: {employee_id}_{fingerprint_number}.tif
    pattern = re.compile(r'(\d+)_(\d+)\.tif$')
    
    # Scan files in the source directory
    for filename in os.listdir(source_dir):
        match = pattern.match(filename)
        if match:
            employee_id = match.group(1)
            fingerprint_num = match.group(2)
            
            # Create employee directory if it doesn't exist
            employee_dir = os.path.join(target_dir, employee_id)
            if not os.path.exists(employee_dir):
                os.makedirs(employee_dir)
            
            # Copy the file to the employee directory
            source_file = os.path.join(source_dir, filename)
            target_file = os.path.join(employee_dir, filename)
            shutil.copy2(source_file, target_file)
            
            # Update data structure
            if employee_id not in employee_data:
                employee_data[employee_id] = []
            
            employee_data[employee_id].append(filename)
    
    # Sort fingerprint numbers for each employee
    for employee_id in employee_data:
        employee_data[employee_id].sort()
    
    return employee_data

if __name__ == "__main__":
    # Replace these with your actual source and target directories
    SOURCE_DIRECTORY = "DB2_B"
    TARGET_DIRECTORY = "fingerprint_adapting_test_dataset"
    
    print(f"Starting to refactor fingerprints from {SOURCE_DIRECTORY} to {TARGET_DIRECTORY}...")
    employee_data = refactor_fingerprint_folders(SOURCE_DIRECTORY, TARGET_DIRECTORY)
    
    # Print summary
    print("\nRefactoring complete!")
    print(f"Organized {sum(len(fingerprints) for fingerprints in employee_data.values())} fingerprints")
    print(f"Found {len(employee_data)} unique employees")
    print(f"Data structure created and saved to {TARGET_DIRECTORY}/fingerprint_summary.txt")
    
    # Example of how to access the data
    print("\nData structure (first 3 employees):")
    for employee_id in list(employee_data.keys())[:3]:
        print(f"Employee ID: {employee_id}, Fingerprints: {employee_data[employee_id]}")
    print(employee_data)