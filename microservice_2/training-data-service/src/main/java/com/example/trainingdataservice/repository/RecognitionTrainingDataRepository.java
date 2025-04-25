package com.example.trainingdataservice.repository;

import com.example.trainingdataservice.model.RecognitionTrainingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecognitionTrainingDataRepository extends JpaRepository<RecognitionTrainingData, String> {
    List<RecognitionTrainingData> findByTrainingDataId(String trainingDataId);
    List<RecognitionTrainingData> findByFingerprintRecognitionModelId(String modelId);
}
