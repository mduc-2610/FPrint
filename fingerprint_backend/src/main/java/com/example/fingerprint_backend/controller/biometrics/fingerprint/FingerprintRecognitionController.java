package com.example.fingerprint_backend.controller.biometrics.fingerprint;

import com.example.fingerprint_backend.model.access.Area;
import com.example.fingerprint_backend.model.biometrics.recognition.Recognition;
import com.example.fingerprint_backend.repository.access.AreaRepository;
import com.example.fingerprint_backend.repository.biometrics.recognition.RecognitionRepository;
import com.example.fingerprint_backend.service.FingerprintRecognitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/fingerprint-recognition")
@RequiredArgsConstructor
public class FingerprintRecognitionController {
    private final FingerprintRecognitionService recognitionService;
    private final AreaRepository areaRepository;
    private final RecognitionRepository recognitionRepository;

    @PostMapping("/recognize")
    public ResponseEntity<?> recognizeFingerprint(
            @RequestParam("file") MultipartFile file,
            @RequestParam("segmentationModelId") String segmentationModelId,
            @RequestParam("recognitionModelId") String recognitionModelId,
            @RequestParam(value = "areaId", required = false) String areaId,
            @RequestParam(value = "accessType", required = false, defaultValue = "ENTRY") String accessType) {
        try {
            Area area = null;
            if (areaId != null && !areaId.isEmpty()) {
                Optional<Area> areaOpt = areaRepository.findById(areaId);
                if (areaOpt.isPresent()) {
                    area = areaOpt.get();
                }
            }

            Map<String, Object> result = recognitionService.processRecognition(
                    file,
                    segmentationModelId,
                    recognitionModelId,
                    area,
                    accessType);


            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
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