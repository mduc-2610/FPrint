package com.example.fingerprint_backend.model.biometrics.fingerprint;

import com.example.fingerprint_backend.model.auth.Employee;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    private String image;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] imageData;

    private String position;
    private LocalDateTime capturedAt;
    private Double quality;
    private Double lastRecognitionConfidence;

    @Builder.Default
    private boolean active = true;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "segmentation_model_id")
    private FingerprintSegmentationModel fingerprintSegmentationModel;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "recognition_model_id")
    private FingerprintRecognitionModel fingerprintRecognitionModel;

    @Version
    private Long version;
}