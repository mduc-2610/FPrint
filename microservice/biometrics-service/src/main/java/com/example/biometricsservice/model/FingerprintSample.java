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
public class FingerprintSample {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String employeeId; // Reference to the employee in User service

    private String image;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] imageData;

    private String position;
    private LocalDateTime capturedAt;
    private Double quality;
    private Double lastRecognitionConfidence;

    @Version
    private Long version;
}

