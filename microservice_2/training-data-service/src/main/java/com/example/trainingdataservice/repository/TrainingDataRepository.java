package com.example.trainingdataservice.repository;

import com.example.trainingdataservice.model.TrainingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrainingDataRepository extends JpaRepository<TrainingData, String> {
    List<TrainingData> findByNameContaining(String name);
    List<TrainingData> findByPurpose(String purpose);
    List<TrainingData> findByCreatedAtAfter(LocalDateTime date);
    List<TrainingData> findBySampleCountGreaterThan(int count);

    List<TrainingData> findByNameContainingIgnoreCase(String name);
    List<TrainingData> findByPurposeContainingIgnoreCase(String purpose);
}
