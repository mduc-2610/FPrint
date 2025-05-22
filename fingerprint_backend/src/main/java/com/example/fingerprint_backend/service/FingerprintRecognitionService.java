package com.example.fingerprint_backend.service;

import com.example.fingerprint_backend.model.*;
import com.example.fingerprint_backend.repository.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FingerprintRecognitionService {

    @Value("${fingerprint.api.url}")
    private String fingerprintApiUrl;

    private final EmployeeRepository employeeRepository;
    private final RecognitionRepository recognitionRepository;
    private final AccessLogRepository accessLogRepository;
    private final AreaAccessRepository areaAccessRepository;
    private final AreaRepository areaRepository;

    @Autowired
    private final RestTemplate restTemplate;

    public RecognitionResult recognizeFingerprint(
            MultipartFile fingerprintImage,
            FingerprintSegmentationModel segmentationModel,
            FingerprintRecognitionModel recognitionModel) throws Exception {

        String segmentationModelPath = segmentationModel.getPathName();
        String recognitionModelPath = recognitionModel.getPathName();

        try {
            byte[] fileBytes = fingerprintImage.getBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return fingerprintImage.getOriginalFilename();
                }
            });
            body.add("segmentation_model_path", segmentationModelPath);
            body.add("recognition_model_path", recognitionModelPath);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    fingerprintApiUrl + "/api/recognize",
                    requestEntity,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.getBody());

                if (rootNode.has("error")) {
                    throw new Exception("Recognition error: " + rootNode.get("error").asText());
                }

                JsonNode similarityNode = rootNode.get("similarity");

                String employeeId = null;
                double confidence = 0.0;
                String fingerprintId = null;
                boolean isMatch = false;

                if (similarityNode != null) {
                    JsonNode employeeIdNode = similarityNode.get("employee_id");
                    if (employeeIdNode != null && !employeeIdNode.isNull()) {
                        employeeId = employeeIdNode.asText();
                    }
                    JsonNode fingerIdNode = similarityNode.get("fingerprint_id");
                    if (fingerIdNode != null && !fingerIdNode.isNull()) {
                        fingerprintId = fingerIdNode.asText();
                    }

                    confidence = similarityNode.get("confidence").asDouble();

                    if (similarityNode.has("match")) {
                        isMatch = similarityNode.get("match").asBoolean();
                    }
                }

                System.out.println("Successfully recognized: employeeId=" + employeeId + ", confidence=" + confidence
                        + " fingerId=" + fingerprintId);

                return new RecognitionResult(employeeId, confidence, fingerprintId, isMatch);
            } else {
                throw new Exception("Failed to recognize fingerprint: " + response.getBody());
            }
        } catch (IOException e) {
            System.err.println("Error in fingerprint recognition process: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Failed to recognize fingerprint: " + e.getMessage(), e);
        }
    }

    @Transactional
    public RecognitionResponse processRecognition(RecognitionRequest request) throws Exception {
        RecognitionResult result = recognizeFingerprint(
                request.getFile(),
                request.getSegmentationModel(),
                request.getRecognitionModel());

        if (result == null) {
            throw new Exception("Fingerprint recognition failed");
        }

        LocalDateTime now = LocalDateTime.now();
        Optional<Area> areaOpt = areaRepository.findById(request.getArea().getId());
        if (areaOpt.isEmpty()) {
            throw new IllegalArgumentException("Area with ID " + request.getArea().getId() + " not found");
        }
        Area area = areaOpt.get();

        AccessLog accessLog = AccessLog.builder()
                .area(area)
                .timestamp(now)
                .accessType(request.getAccessType())
                .build();

        Employee employee = null;
        boolean isAccessible = false;

        if (result.isMatch() && result.getEmployeeId() != null) {
            Optional<Employee> employeeOpt = employeeRepository.findById(result.getEmployeeId());

            if (employeeOpt.isPresent()) {
                employee = employeeOpt.get();
                accessLog.setEmployee(employee);

                List<AreaAccess> areaAccessList = areaAccessRepository.findByEmployeeId(employee.getId());
                isAccessible = areaAccessList.stream()
                        .anyMatch(areaAccess -> areaAccess.getArea().getId().equals(area.getId()));

                accessLog.setAuthorized(isAccessible);
            } else {
                accessLog.setAuthorized(false);
                accessLog.setEmployee(null);
            }
        } else {
            accessLog.setAuthorized(false);
            accessLog.setEmployee(null);
        }

        AccessLog savedAccessLog = accessLogRepository.save(accessLog);

        try {
            Recognition recognition = Recognition.builder()
                    .employee(employee)
                    .accessLog(savedAccessLog)
                    .fingerprintSegmentationModel(request.getSegmentationModel())
                    .fingerprintRecognitionModel(request.getRecognitionModel())
                    .timestamp(now)
                    .confidence((float) result.getConfidence())
                    .build();

            recognitionRepository.save(recognition);

        } catch (Exception e) {
            System.err.println("Failed to create recognition record: " + e.getMessage());
            e.printStackTrace();
        }

        RecognitionResponse.RecognitionResponseBuilder responseBuilder = RecognitionResponse.builder()
                .matched(result.isMatch())
                .confidence(result.getConfidence())
                .accessLog(savedAccessLog)
                .authorized(savedAccessLog.isAuthorized());

        if (result.isMatch() && employee != null) {
            responseBuilder.accessable(isAccessible);
            responseBuilder.employeeId(result.getEmployeeId());
            responseBuilder.employee(employee);
        }

        return responseBuilder.build();
    }
}