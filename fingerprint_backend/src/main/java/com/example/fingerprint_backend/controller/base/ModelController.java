package com.example.fingerprint_backend.controller.base;


import com.example.fingerprint_backend.model.analytics.ModelStatistics;
import com.example.fingerprint_backend.model.base.Model;
import com.example.fingerprint_backend.repository.base.ModelRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

public abstract class ModelController<T, ID, R> {
    protected final ModelRepository<T, ID> repository;

    public ModelController(ModelRepository<T, ID> repository) {
        this.repository = repository;
    }

    @GetMapping("")
    public List<T> getAll() { return repository.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<T> getById(@PathVariable ID id) {
        Optional<T> model = repository.findById(id);
        return model.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<T> getByName(@PathVariable String name) {
        Optional<T> model = repository.findByName(name);
        return model.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/version/{version}")
    public ResponseEntity<List<T>> getByVersion(@PathVariable String version) {
        return ResponseEntity.ok(repository.findByVersion(version));
    }

    @GetMapping("/accuracy/{minAccuracy}")
    public ResponseEntity<List<T>> getByAccuracyGreaterThan(@PathVariable float minAccuracy) {
        return ResponseEntity.ok(repository.findByAccuracyGreaterThanEqual(minAccuracy));
    }

    @GetMapping("/latest")
    public ResponseEntity<T> getLatestModel() {
        Optional<T> model = repository.findTopByOrderByCreatedAtDesc();
        return model.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<T> createModel(@RequestBody T model) {
        return ResponseEntity.ok(((org.springframework.data.jpa.repository.JpaRepository<T, ID>) repository).save(model));
    }

    @PutMapping("/{id}")
    public ResponseEntity<T> updateModel(@PathVariable ID id, @RequestBody T updatedModel) {
        if (((org.springframework.data.jpa.repository.JpaRepository<T, ID>) repository).existsById(id)) {
            return ResponseEntity.ok(((org.springframework.data.jpa.repository.JpaRepository<T, ID>) repository).save(updatedModel));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModel(@PathVariable ID id) {
        if (((org.springframework.data.jpa.repository.JpaRepository<T, ID>) repository).existsById(id)) {
            ((org.springframework.data.jpa.repository.JpaRepository<T, ID>) repository).deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<ModelStatistics> getModelStatistics(@PathVariable ID id) {
        return repository.findById(id)
                .map( model -> {
                    ModelStatistics statistics = ModelStatistics.builder()
                            .model((Model) model)
                            .totalUsage(calculateTotalUsage((Model) model))
                            .averageConfidence(calculateAverageConfidence((Model) model))
                            .build();

                    return ResponseEntity.ok(statistics);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/statistics")
    public ResponseEntity<List<ModelStatistics>> getAllModelStatistics() {
        List<T> models = repository.findAll();

        List<ModelStatistics> statisticsList = models.stream()
                .map(model -> {
                    ModelStatistics statistics = ModelStatistics.builder()
                            .model((Model) model)
                            .totalUsage(calculateTotalUsage((Model) model))
                            .averageConfidence(calculateAverageConfidence((Model) model))
                            .build();
                    return statistics;
                })
                .toList();

        return ResponseEntity.ok(statisticsList);
    }

    protected int calculateTotalUsage(Model model) {
        return 0;
    }

    protected float calculateAverageConfidence(Model model) {
        return 0f;
    }
}
