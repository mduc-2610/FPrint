package com.example.modelmanagementservice.service;

import com.example.modelmanagementservice.model.Model;
import com.example.modelmanagementservice.model.ModelStatistics;
import com.example.modelmanagementservice.repository.ModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ModelService {
    @Autowired
    private ModelRepository modelRepository;

    public List<Model> getAllModels() {
        return modelRepository.findAll();
    }

    public Optional<Model> getModelById(String id) {
        return modelRepository.findById(id);
    }

    public Model createModel(Model model) {
        model.setCreatedAt(LocalDateTime.now());
        model.setUpdatedAt(LocalDateTime.now());
        return modelRepository.save(model);
    }

    public Model updateModel(Model model) {
        model.setUpdatedAt(LocalDateTime.now());
        return modelRepository.save(model);
    }

    public void deleteModel(String id) {
        modelRepository.deleteById(id);
    }

    public List<Model> findModelsByName(String name) {
        return modelRepository.findByNameContaining(name);
    }

    public List<Model> findModelsByVersion(String version) {
        return modelRepository.findByVersion(version);
    }

    public ModelStatistics getModelStatistics(String modelId) {
        // This would typically use the BiometricsClient to get real statistics
        // For simplicity, we're returning mock data
        Optional<Model> modelOpt = modelRepository.findById(modelId);
        if (!modelOpt.isPresent()) {
            return null;
        }

        Model model = modelOpt.get();

        return ModelStatistics.builder()
                .modelId(modelId)
                .modelName(model.getName())
                .totalUsage(100)
                .averageConfidence(0.85f)
                .lastUsed(LocalDateTime.now().minusDays(1))
                .build();
    }
}
