package com.example.fingerprint_backend.controller;

import com.example.fingerprint_backend.model.*;
import com.example.fingerprint_backend.service.FingerprintRecognitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fingerprint-recognition")
@RequiredArgsConstructor
public class FingerprintRecognitionController {
    private final FingerprintRecognitionService recognitionService;

    @PostMapping(value = "/recognize", consumes = "multipart/form-data")
    public ResponseEntity<?> recognizeFingerprint(
            @ModelAttribute RecognitionRequest recognitionRequest) {
        try {

            RecognitionResponse result = recognitionService.processRecognition(recognitionRequest);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }
}