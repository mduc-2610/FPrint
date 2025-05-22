package com.example.fingerprint_backend.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RecognitionRequest {
    private transient MultipartFile file;
    private FingerprintSegmentationModel segmentationModel;
    private FingerprintRecognitionModel recognitionModel;
    private Area area;
    private String accessType = "ENTRY";
}
