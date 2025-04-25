package com.example.trainingdataservice.service;

import com.example.trainingdataservice.client.ModelManagementClient;
import com.example.trainingdataservice.model.SegmentationTrainingData;
import com.example.trainingdataservice.model.TrainingData;
import com.example.trainingdataservice.repository.SegmentationTrainingDataRepository;
import com.example.trainingdataservice.repository.TrainingDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SegmentationTrainingDataService {
    @Autowired
    private SegmentationTrainingDataRepository segmentationDataRepository;

    @Autowired
    private TrainingDataRepository trainingDataRepository;

    @Autowired
    private ModelManagementClient modelManagementClient;

    public List<SegmentationTrainingData> getAllSegmentationData() {
        return segmentationDataRepository.findAll();
    }

    public Optional<SegmentationTrainingData> getSegmentationDataById(String id) {
        return segmentationDataRepository.findById(id);
    }

    public List<SegmentationTrainingData> getSegmentationDataByTrainingDataId(String trainingDataId) {
        return segmentationDataRepository.findByTrainingDataId(trainingDataId);
    }

    public List<SegmentationTrainingData> getSegmentationDataByModelId(String modelId) {
        return segmentationDataRepository.findByFingerprintSegmentationModelId(modelId);
    }

    public SegmentationTrainingData createSegmentationData(SegmentationTrainingData data, String trainingDataId) {
        // Verify training data exists
        Optional<TrainingData> trainingDataOpt = trainingDataRepository.findById(trainingDataId);
        if (!trainingDataOpt.isPresent()) {
            throw new RuntimeException("Training data not found");
        }

        // Verify model exists
        ResponseEntity<Object> response = modelManagementClient.getSegmentationModelById(data.getFingerprintSegmentationModelId());
        if (response.getStatusCodeValue() != 200) {
            throw new RuntimeException("Segmentation model not found");
        }

        // Set ID and training data
        if (data.getId() == null) {
            data.setId(UUID.randomUUID().toString());
        }
        data.setTrainingData(trainingDataOpt.get());

        return segmentationDataRepository.save(data);
    }

    public SegmentationTrainingData updateSegmentationData(SegmentationTrainingData data) {
        return segmentationDataRepository.save(data);
    }

    public void deleteSegmentationData(String id) {
        segmentationDataRepository.deleteById(id);
    }
}
