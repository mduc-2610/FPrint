package com.example.biometricsservice.repository;

import com.example.biometricsservice.model.FingerprintSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FingerprintSampleRepository extends JpaRepository<FingerprintSample, String> {
    List<FingerprintSample> findByEmployeeId(String employeeId);
    List<FingerprintSample> findByPosition(String position);
    List<FingerprintSample> findByQualityGreaterThan(Double quality);
    List<FingerprintSample> findByCapturedAtBetween(LocalDateTime start, LocalDateTime end);
    List<FingerprintSample> findBySegmentationModelId(String modelId);
    List<FingerprintSample> findByRecognitionModelId(String modelId);
}