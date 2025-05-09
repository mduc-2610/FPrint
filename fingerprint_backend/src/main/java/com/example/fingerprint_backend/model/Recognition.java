package com.example.fingerprint_backend.model;

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
public class Recognition {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "fingerprint_recognition_model_id")
    private FingerprintRecognitionModel fingerprintRecognitionModel;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "fingerprint_region_model_id")
    private FingerprintSegmentationModel fingerprintSegmentationModel;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "access_log_id")
    private AccessLog accessLog;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private LocalDateTime timestamp;
    private float confidence;
}

