package com.example.modelmanagementservice.repository;

import com.example.modelmanagementservice.model.FingerprintSegmentationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FingerprintSegmentationModelRepository extends JpaRepository<FingerprintSegmentationModel, String> {
    List<FingerprintSegmentationModel> findByAccuracyGreaterThan(float accuracy);
    List<FingerprintSegmentationModel> findByCreatedAtAfter(LocalDateTime date);
}
