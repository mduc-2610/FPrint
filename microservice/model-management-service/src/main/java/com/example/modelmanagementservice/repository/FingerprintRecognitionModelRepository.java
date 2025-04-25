package com.example.modelmanagementservice.repository;

import com.example.modelmanagementservice.model.FingerprintRecognitionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FingerprintRecognitionModelRepository extends JpaRepository<FingerprintRecognitionModel, String> {
    List<FingerprintRecognitionModel> findByAccuracyGreaterThan(float accuracy);
    List<FingerprintRecognitionModel> findByCreatedAtAfter(LocalDateTime date);
}
