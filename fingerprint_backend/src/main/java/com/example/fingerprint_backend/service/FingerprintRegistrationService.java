package com.example.fingerprint_backend.service;

import com.example.fingerprint_backend.model.auth.Employee;
import com.example.fingerprint_backend.model.biometrics.fingerprint.FingerprintSample;
import com.example.fingerprint_backend.model.biometrics.fingerprint.FingerprintSegmentationModel;
import com.example.fingerprint_backend.model.biometrics.fingerprint.FingerprintRecognitionModel;
import com.example.fingerprint_backend.repository.auth.EmployeeRepository;
import com.example.fingerprint_backend.repository.biometrics.fingerprint.FingerprintSampleRepository;
import com.example.fingerprint_backend.repository.biometrics.fingerprint.FingerprintSegmentationModelRepository;
import com.example.fingerprint_backend.repository.biometrics.fingerprint.FingerprintRecognitionModelRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FingerprintRegistrationService {

    @Value("${fingerprint.api.url:http://localhost:5000}")
    private String fingerprintApiUrl;

    private final EmployeeRepository employeeRepository;
    private final FingerprintSampleRepository fingerprintSampleRepository;
    private final FingerprintSegmentationModelRepository segmentationModelRepository;
    private final FingerprintRecognitionModelRepository recognitionModelRepository;

    @Autowired
    private final RestTemplate restTemplate;

    @Transactional
    public FingerprintSample registerFingerprint(
            String employeeId,
            MultipartFile file,
            String position,
            String segmentationModelId,
            String recognitionModelId) throws Exception {

        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        Employee employee = employeeOpt
                .orElseThrow(() -> new Exception("Employee with ID " + employeeId + " not found"));

        Optional<FingerprintSegmentationModel> segModelOpt = segmentationModelRepository.findById(segmentationModelId);
        FingerprintSegmentationModel segmentationModel = segModelOpt
                .orElseThrow(() -> new Exception("Segmentation model with ID " + segmentationModelId + " not found"));

        Optional<FingerprintRecognitionModel> recModelOpt = recognitionModelRepository.findById(recognitionModelId);
        FingerprintRecognitionModel recognitionModel = recModelOpt
                .orElseThrow(() -> new Exception("Recognition model with ID " + recognitionModelId + " not found"));

        String segmentationModelPath = segmentationModel.getPathName();
        String recognitionModelPath = recognitionModel.getPathName();

        try {
            byte[] fileBytes = file.getBytes();

            // Lưu mẫu vân tay vào cơ sở dữ liệu
            FingerprintSample sample = FingerprintSample.builder()
                    .employee(employee)
                    .image(file.getOriginalFilename())
                    .imageData(fileBytes)
                    .position(position)
                    .capturedAt(LocalDateTime.now())
                    .fingerprintSegmentationModel(segmentationModel)
                    .fingerprintRecognitionModel(recognitionModel)
                    .quality(1.0)
                    .active(true)
                    .build();

            FingerprintSample savedSample = fingerprintSampleRepository.save(sample);

            // Gửi yêu cầu đăng ký vân tay đến API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            body.add("fingerprint_id", sample.getId());
            body.add("employee_id", employeeId);
            body.add("position", position);
            body.add("segmentation_model_path", segmentationModelPath);
            body.add("recognition_model_path", recognitionModelPath);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    fingerprintApiUrl + "/api/register",
                    requestEntity,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return savedSample;
            } else {
                throw new Exception("Failed to register fingerprint: " + response.getBody());
            }

        } catch (IOException e) {
            throw new Exception("Failed to save fingerprint image: " + e.getMessage(), e);
        }
    }
}