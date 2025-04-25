package com.example.trainingdataservice.repository;

import com.example.trainingdataservice.model.SegmentationTrainingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SegmentationTrainingDataRepository extends JpaRepository<SegmentationTrainingData, String> {
    List<SegmentationTrainingData> findByTrainingDataId(String trainingDataId);
    List<SegmentationTrainingData> findByFingerprintSegmentationModelId(String modelId);
}
