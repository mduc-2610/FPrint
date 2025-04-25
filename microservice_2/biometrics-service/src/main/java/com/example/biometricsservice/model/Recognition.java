package com.example.biometricsservice.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recognition {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String cameraImageId; // Reference to the camera image in Access Control service
    private String employeeId; // Reference to the employee in User Management service
    private String accessLogId; // Reference to the access log in Access Control service

    private String fingerprintRecognitionModelId; // Reference to the recognition model in Model Management service
    private String fingerprintSegmentationModelId; // Reference to the segmentation model in Model Management service

    private LocalDateTime timestamp;
    private float confidence;
}
