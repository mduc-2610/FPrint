package com.example.trainingdataservice.service;

import com.example.trainingdataservice.model.TrainingData;
import com.example.trainingdataservice.repository.TrainingDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TrainingDataService {
    @Autowired
    private TrainingDataRepository trainingDataRepository;

    public List<TrainingData> getAllTrainingData() {
        return trainingDataRepository.findAll();
    }

    public Optional<TrainingData> getTrainingDataById(String id) {
        return trainingDataRepository.findById(id);
    }

    public TrainingData createTrainingData(TrainingData trainingData) {
        trainingData.setCreatedAt(LocalDateTime.now());
        trainingData.setUpdatedAt(LocalDateTime.now());
        return trainingDataRepository.save(trainingData);
    }

    public TrainingData updateTrainingData(TrainingData trainingData) {
        trainingData.setUpdatedAt(LocalDateTime.now());
        return trainingDataRepository.save(trainingData);
    }

    public void deleteTrainingData(String id) {
        trainingDataRepository.deleteById(id);
    }

    public List<TrainingData> findBySampleCountGreaterThan(int count) {
        return trainingDataRepository.findBySampleCountGreaterThan(count);
    }

    public List<TrainingData> findByName(String name) {
        return trainingDataRepository.findByNameContainingIgnoreCase(name);
    }

    public List<TrainingData> findByPurpose(String purpose) {
        return trainingDataRepository.findByPurposeContainingIgnoreCase(purpose);
    }

    public List<TrainingData> findByCreatedAfter(LocalDateTime timestamp) {
        return trainingDataRepository.findByCreatedAtAfter(timestamp);
    }
}
