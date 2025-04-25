package com.example.modelmanagementservice.service;

import com.example.modelmanagementservice.model.FingerprintSegmentationModel;
import com.example.modelmanagementservice.repository.FingerprintSegmentationModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FingerprintSegmentationModelService {
    @Autowired
    private FingerprintSegmentationModelRepository repository;

    @Autowired
    private ModelService modelService;

    public List<FingerprintSegmentationModel> getAllModels() {
        return repository.findAll();
    }

    public Optional<FingerprintSegmentationModel> getModelById(String id) {
        return repository.findById(id);
    }

    public FingerprintSegmentationModel createModel(FingerprintSegmentationModel model) {
        return repository.save((FingerprintSegmentationModel) modelService.createModel(model));
    }

    public FingerprintSegmentationModel updateModel(FingerprintSegmentationModel model) {
        return repository.save((FingerprintSegmentationModel) modelService.updateModel(model));
    }

    public void deleteModel(String id) {
        repository.deleteById(id);
    }

    public List<FingerprintSegmentationModel> findByAccuracyGreaterThan(float accuracy) {
        return repository.findByAccuracyGreaterThan(accuracy);
    }

    public List<FingerprintSegmentationModel> findByCreatedAfter(LocalDateTime date) {
        return repository.findByCreatedAtAfter(date);
    }
}