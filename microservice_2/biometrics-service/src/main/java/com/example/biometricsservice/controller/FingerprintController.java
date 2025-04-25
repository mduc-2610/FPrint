package com.example.biometricsservice.controller;

import com.example.biometricsservice.model.FingerprintSample;
import com.example.biometricsservice.repository.FingerprintSampleRepository;
import com.example.biometricsservice.service.FingerprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@RequestMapping("/api/fingerprint")
public class FingerprintController {

    @Autowired
    private FingerprintService fingerprintService;

    @Autowired
    private FingerprintSampleRepository fingerprintSampleRepository;
    private static final Logger logger = LoggerFactory.getLogger(FingerprintController.class);

    @GetMapping
    public ResponseEntity<List<FingerprintSample>> getAllFingerprints() {
        return ResponseEntity.ok(fingerprintSampleRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FingerprintSample> getFingerprintById(@PathVariable String id) {
        return fingerprintSampleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<FingerprintSample>> getFingerprintsByEmployeeId(@PathVariable String employeeId) {
        return ResponseEntity.ok(fingerprintSampleRepository.findByEmployeeId(employeeId));
    }

    @PostMapping("/register/{employeeId}/single")
    public ResponseEntity<FingerprintSample> registerFingerprint(
            @PathVariable("employeeId") String employeeId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("position") String position,
            @RequestParam("segmentationModelId") String segmentationModelId,
            @RequestParam("recognitionModelId") String recognitionModelId) {

        try {
            FingerprintSample sample = fingerprintService.registerFingerprint(
                    employeeId,
                    file,
                    position,
                    segmentationModelId,
                    recognitionModelId
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(sample);
        } catch (Exception e) {
            logger.error("Unexpected error during fingerprint recognition", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFingerprint(@PathVariable String id) {
        fingerprintSampleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-recognition-model/{modelId}")
    public ResponseEntity<List<FingerprintSample>> getFingerprintsByRecognitionModel(@PathVariable String modelId) {
        List<FingerprintSample> samples = fingerprintSampleRepository.findByRecognitionModelId(modelId);
        return ResponseEntity.ok(samples);
    }
    
    @GetMapping("/by-segmentation-model/{modelId}")
    public ResponseEntity<List<FingerprintSample>> getFingerprintsBySegmentationModel(@PathVariable String modelId) {
        List<FingerprintSample> samples = fingerprintSampleRepository.findBySegmentationModelId(modelId);
        return ResponseEntity.ok(samples);
    }
}
