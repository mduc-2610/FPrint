package com.example.modelmanagementservice.repository;


import java.time.LocalDateTime;
import java.util.List;

import com.example.modelmanagementservice.repository.ModelRepository;
import com.example.modelmanagementservice.model.FingerprintSegmentationModel;

import org.springframework.stereotype.Repository;

@Repository
public interface FingerprintSegmentationModelRepository 
    extends ModelRepository<FingerprintSegmentationModel> {
    
    List<FingerprintSegmentationModel> findByAccuracyGreaterThan(float threshold);
    List<FingerprintSegmentationModel> findByCreatedAtAfter(LocalDateTime date);
}
