FROM python:3.9-slim

WORKDIR /app

# Cài đặt các gói phụ thuộc
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgl1-mesa-glx \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# Sao chép requirements
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Sao chép mã nguồn
COPY fingerprint_api.py .

# Tạo các thư mục cần thiết
RUN mkdir -p temp_uploads \
    fingerprint_models/recognition \
    fingerprint_models/segmentation \
    fingerprint_adapting_dataset \
    fingerprint_adapting_models \
    processed_fingerprints

# Mở cổng
EXPOSE 5000

# Khởi chạy ứng dụng
CMD ["python", "fingerprint_api.py"]