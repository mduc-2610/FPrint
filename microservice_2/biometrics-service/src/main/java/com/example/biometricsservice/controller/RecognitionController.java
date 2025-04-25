package com.example.biometricsservice.controller;

import com.example.biometricsservice.client.ModelManagementClient;
import com.example.biometricsservice.model.Recognition;
import com.example.biometricsservice.model.RecognitionResult;
import com.example.biometricsservice.repository.RecognitionRepository;
import com.example.biometricsservice.service.FingerprintService;

import com.example.biometricsservice.client.AccessControlClient;
import com.example.biometricsservice.client.UserManagementClient;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recognition")
public class RecognitionController {
    private static final Logger logger = LoggerFactory.getLogger(RecognitionController.class);

    @Autowired
    private FingerprintService fingerprintService;

    @Autowired
    private RecognitionRepository recognitionRepository;

    @Autowired
    private AccessControlClient accessControlClient;
    
    @Autowired
    private UserManagementClient userManagementClient;

    @GetMapping
    public ResponseEntity<List<Recognition>> getAllRecognitions() {
        return ResponseEntity.ok(recognitionRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recognition> getRecognitionById(@PathVariable String id) {
        return recognitionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Recognition>> getRecognitionsByEmployeeId(@PathVariable String employeeId) {
        return ResponseEntity.ok(recognitionRepository.findByEmployeeId(employeeId));
    }

    @GetMapping("/time-range")
    public ResponseEntity<List<Recognition>> getRecognitionsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(recognitionRepository.findByTimestampBetween(start, end));
    }
    
    @PostMapping("/recognize")
    public ResponseEntity<?> recognizeFingerprint(
            @RequestParam("file") MultipartFile file,
            @RequestParam("segmentationModelId") String segmentationModelId,
            @RequestParam("recognitionModelId") String recognitionModelId,
            @RequestParam(value = "areaId") String areaId,
            @RequestParam(value = "accessType", required = false, defaultValue = "ENTRY") String accessType) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "File is empty",
                "status", "BAD_REQUEST"
            ));
        }

        try {
            Object area = null;
            if (areaId != null && !areaId.isEmpty()) {
                ResponseEntity<Object> areaResponse = accessControlClient.getAreaById(areaId);
                if (areaResponse.getStatusCode().is2xxSuccessful() && areaResponse.getBody() != null) {
                    area = areaResponse.getBody();
                    logger.info("Area found with ID: {}", areaId);

                } else {
                    logger.warn("Area not found with ID: {}", areaId);
                }
            }

            RecognitionResult result = fingerprintService.recognizeFingerprint(
                file, 
                segmentationModelId, 
                recognitionModelId
            );

            if (result == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Fingerprint recognition failed",
                        "status", "RECOGNITION_ERROR"
                    ));
            }

            Object accessLog = fingerprintService.createAccessLog(
                result.getEmployeeId(), 
                area, 
                accessType, 
                result.isMatch(), 
                result.getConfidence(),
                segmentationModelId,
                recognitionModelId
            );

            if (result.isMatch()) {
                ResponseEntity<Object> employeeResponse = userManagementClient
                    .getEmployeeById(result.getEmployeeId());
                
                Object employee = employeeResponse.getStatusCode().is2xxSuccessful() 
                    ? employeeResponse.getBody() 
                    : null;

                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("matched", true);
                responseBody.put("employeeId", result.getEmployeeId());
                responseBody.put("confidence", result.getConfidence());
                responseBody.put("accessLog", accessLog);
                
                if (employee != null) {
                    responseBody.put("employee", employee);
                    responseBody.put("authorized", isAuthorized(accessLog));
                } else {
                    responseBody.put("message", "Employee not found in the database but fingerprint matched");
                    responseBody.put("authorized", false);
                }

                return ResponseEntity.ok(responseBody);
            } 
            else {
                return ResponseEntity.ok(Map.of(
                    "matched", false,
                    "confidence", result.getConfidence(),
                    "accessLog", accessLog,
                    "authorized", false
                ));
            }
        } 
        catch (FeignException fe) {
            logger.error("Microservice communication error", fe);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                    "error", "Service communication failed: " + fe.getMessage(),
                    "status", "SERVICE_UNAVAILABLE"
                ));
        } catch (Exception e) {
            logger.error("Unexpected error during fingerprint recognition", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "An unexpected error occurred: " + e.getMessage(),
                    "status", "INTERNAL_ERROR"
                ));
        }
    }


    private boolean isAuthorized(Object accessLog) {
        return accessLog != null; 
    }

    
    @GetMapping("/by-recognition-model/{modelId}")
    public ResponseEntity<List<Recognition>> getRecognitionsByRecognitionModel(@PathVariable String modelId) {
        List<Recognition> recognitions = recognitionRepository.findByFingerprintRecognitionModelId(modelId);
        return ResponseEntity.ok(recognitions);
    }
    
    @GetMapping("/by-segmentation-model/{modelId}")
    public ResponseEntity<List<Recognition>> getRecognitionsBySegmentationModel(@PathVariable String modelId) {
        List<Recognition> recognitions = recognitionRepository.findByFingerprintSegmentationModelId(modelId);
        return ResponseEntity.ok(recognitions);
    }
    
}
