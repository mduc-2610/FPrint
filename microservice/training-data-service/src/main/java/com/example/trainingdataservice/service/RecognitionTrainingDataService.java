package com.example.trainingdataservice.service;

import com.example.trainingdataservice.client.ModelManagementClient;
import com.example.trainingdataservice.model.RecognitionTrainingData;
import com.example.trainingdataservice.model.TrainingData;
import com.example.trainingdataservice.repository.RecognitionTrainingDataRepository;
import com.example.trainingdataservice.repository.TrainingDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RecognitionTrainingDataService {
    @Autowired
    private RecognitionTrainingDataRepository recognitionDataRepository;

    @Autowired
    private TrainingDataRepository trainingDataRepository;

    @Autowired
    private ModelManagementClient modelManagementClient;

    public List<RecognitionTrainingData> getAllRecognitionData() {
        return recognitionDataRepository.findAll();
    }

    public Optional<RecognitionTrainingData> getRecognitionDataById(String id) {
        return recognitionDataRepository.findById(id);
    }

    public List<RecognitionTrainingData> getRecognitionDataByTrainingDataId(String trainingDataId) {
        return recognitionDataRepository.findByTrainingDataId(trainingDataId);
    }

    public List<RecognitionTrainingData> getRecognitionDataByModelId(String modelId) {
        return recognitionDataRepository.findByFingerprintRecognitionModelId(modelId);
    }

    public RecognitionTrainingData createRecognitionData(RecognitionTrainingData data, String trainingDataId) {
        // Verify training data exists
        Optional<TrainingData> trainingDataOpt = trainingDataRepository.findById(trainingDataId);
        if (!trainingDataOpt.isPresent()) {
            throw new RuntimeException("Training data not found");
        }

        // Verify model exists
        ResponseEntity<Object> response = modelManagementClient.getRecognitionModelById(data.getFingerprintRecognitionModelId());
        if (response.getStatusCodeValue() != 200) {
            throw new RuntimeException("Recognition model not found");
        }

        // Set ID and training data
        if (data.getId() == null) {
            data.setId(UUID.randomUUID().toString());
        }
        data.setTrainingData(trainingDataOpt.get());

        return recognitionDataRepository.save(data);
    }

    public RecognitionTrainingData updateRecognitionData(RecognitionTrainingData data) {
        return recognitionDataRepository.save(data);
    }

    public void deleteRecognitionData(String id) {
        recognitionDataRepository.deleteById(id);
    }
}
