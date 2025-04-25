package com.example.biometricsservice.repository;

import com.example.biometricsservice.model.Recognition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecognitionRepository extends JpaRepository<Recognition, String> {
    List<Recognition> findByEmployeeId(String employeeId);
    List<Recognition> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<Recognition> findByConfidenceGreaterThan(float confidence);
    List<Recognition> findByFingerprintRecognitionModelId(String modelId);
    List<Recognition> findByFingerprintSegmentationModelId(String modelId);
}

