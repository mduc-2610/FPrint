package com.example.modelmanagementservice.repository;

import com.example.modelmanagementservice.repository.ModelRepository;
import com.example.modelmanagementservice.model.FingerprintRecognitionModel;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface FingerprintRecognitionModelRepository 
    extends ModelRepository<FingerprintRecognitionModel> {
    
    List<FingerprintRecognitionModel> findByAccuracyGreaterThan(float threshold);
    List<FingerprintRecognitionModel> findByCreatedAtAfter(LocalDateTime date);
}
