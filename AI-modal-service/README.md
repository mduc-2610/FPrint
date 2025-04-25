# Hướng dẫn chuyển đổi và triển khai hệ thống nhận dạng vân tay

## Tổng quan

Tài liệu này mô tả cách chuyển đổi hệ thống nhận dạng vân tay từ mô hình gọi Process (ProcessBuilder) sang mô hình Webservice, giúp:

- Tăng tính module hóa và linh hoạt trong kiến trúc hệ thống
- Tách biệt những quan tâm giữa các phần của hệ thống, dễ dàng mở rộng
- Dễ dàng scale và quản lý tài nguyên
- Triển khai độc lập phần Python xử lý vân tay và phần Java quản lý business logic

## Cấu trúc thư mục

```
/
├── fingerprint-api/                  # Thư mục chứa mã nguồn Python API
│   ├── Dockerfile                    # Dockerfile cho Python API
│   ├── fingerprint_api.py            # Mã nguồn chính của Python API
│   └── requirements.txt              # Các phụ thuộc của Python API
├── fingerprint_models/               # Thư mục chứa các model đã huấn luyện
│   ├── recognition/                  # Các model nhận dạng
│   └── segmentation/                 # Các model phân đoạn
├── fingerprint_adapting_dataset/     # Thư mục dữ liệu vân tay
├── fingerprint_adapting_models/      # Thư mục chứa các model đã điều chỉnh
├── processed_fingerprints/           # Thư mục chứa vân tay đã xử lý
└── docker-compose.yml                # Cấu hình Docker Compose
```

## Các bước triển khai

### 1. Chuẩn bị môi trường

Để triển khai hệ thống, bạn cần cài đặt các phần mềm sau:

- Docker và Docker Compose
- Java 11 hoặc cao hơn
- Maven hoặc Gradle (tùy thuộc vào dự án Java của bạn)

### 2. Triển khai Python API

1. Tạo thư mục `fingerprint-api` và đặt các file sau vào thư mục:
   - `fingerprint_api.py` (từ file đã chuyển đổi)
   - `requirements.txt`
   - `Dockerfile`

2. Đảm bảo cấu trúc thư mục như đã mô tả ở trên, bao gồm:
   - `fingerprint_models`
   - `fingerprint_adapting_dataset`
   - `fingerprint_adapting_models`
   - `processed_fingerprints`

3. Tạo file `docker-compose.yml` ở thư mục gốc

4. Khởi động container Python API:
   ```
   docker-compose up -d
   ```

### 3. Cập nhật ứng dụng Java

1. Thêm các phụ thuộc Spring RestTemplate vào file `pom.xml` hoặc `build.gradle`:

   ```xml
   <!-- Cho Maven -->
   <dependency>
       <groupId>org.springframework</groupId>
       <artifactId>spring-web</artifactId>
   </dependency>
   ```

2. Thêm class `RestTemplateConfig.java` vào package `config`

3. Cập nhật `FingerprintService.java` với phiên bản đã chuyển đổi

4. Cập nhật file `application.properties` với các cấu hình đã cung cấp

5. Biên dịch và triển khai ứng dụng Spring Boot:
   ```
   ./mvnw clean package
   java -jar target/your-application.jar
   ```

## Kiểm tra hệ thống

### 1. Kiểm tra API Python

Kiểm tra xem API Python có hoạt động không:

```
curl http://localhost:5000/health
```

Kết quả trả về nên là:
```json
{"status": "ok", "message": "Fingerprint API is running"}
```

### 2. Kiểm tra tích hợp

Sử dụng ứng dụng Spring Boot để thực hiện các thao tác:
- Đăng ký vân tay mới
- Nhận dạng vân tay

## Xử lý sự cố

### API Python không hoạt động

1. Kiểm tra logs:
   ```
   docker-compose logs fingerprint-api
   ```

2. Đảm bảo tất cả thư mục dữ liệu và mô hình tồn tại và có quyền truy cập đúng

### Ứng dụng Java không kết nối được với API

1. Kiểm tra cấu hình `fingerprint.api.url` trong `application.properties`
2. Kiểm tra xem API Python có hoạt động trên cổng được chỉ định không
3. Kiểm tra logs của ứng dụng Spring Boot

## Ưu điểm của kiến trúc mới

1. **Tách biệt quan tâm**: Python xử lý AI/ML, Java xử lý business logic
2. **Hiệu suất tốt hơn**: Không phải khởi tạo quá trình Python mới cho mỗi yêu cầu
3. **Khả năng mở rộng**: Có thể dễ dàng scale API Python độc lập với ứng dụng Java
4. **Bảo trì dễ dàng**: Có thể cập nhật/nâng cấp từng phần của hệ thống độc lập
5. **Khả năng phục hồi cao hơn**: Nếu một phần gặp sự cố, phần còn lại vẫn có thể hoạt động