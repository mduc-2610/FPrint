package com.example.modelmanagementservice.service;

import com.example.modelmanagementservice.model.FingerprintRecognitionModel;
import com.example.modelmanagementservice.repository.FingerprintRecognitionModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FingerprintRecognitionModelService {
    @Autowired
    private FingerprintRecognitionModelRepository repository;

    @Autowired
    private ModelService modelService;

    public List<FingerprintRecognitionModel> getAllModels() {
        return repository.findAll();
    }

    public Optional<FingerprintRecognitionModel> getModelById(String id) {
        return repository.findById(id);
    }

    public FingerprintRecognitionModel createModel(FingerprintRecognitionModel model) {
        return repository.save((FingerprintRecognitionModel) modelService.createModel(model));
    }

    public FingerprintRecognitionModel updateModel(FingerprintRecognitionModel model) {
        return repository.save((FingerprintRecognitionModel) modelService.updateModel(model));
    }

    public void deleteModel(String id) {
        repository.deleteById(id);
    }

    public List<FingerprintRecognitionModel> findByAccuracyGreaterThan(float accuracy) {
        return repository.findByAccuracyGreaterThan(accuracy);
    }

    public List<FingerprintRecognitionModel> findByCreatedAfter(LocalDateTime date) {
        return repository.findByCreatedAtAfter(date);
    }
}
