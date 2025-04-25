package com.example.modelmanagementservice.controller;

import com.example.modelmanagementservice.model.Model;
import com.example.modelmanagementservice.model.ModelStatistics;
import com.example.modelmanagementservice.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/models")
public class ModelController {
    @Autowired
    private ModelService modelService;

    @GetMapping
    public ResponseEntity<List<Model>> getAllModels() {
        return ResponseEntity.ok(modelService.getAllModels());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Model> getModelById(@PathVariable String id) {
        return modelService.getModelById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<ModelStatistics> getModelStatistics(@PathVariable String id) {
        ModelStatistics statistics = modelService.getModelStatistics(id);
        if (statistics == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Model>> searchModels(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String version) {

        if (name != null) {
            return ResponseEntity.ok(modelService.findModelsByName(name));
        } else if (version != null) {
            return ResponseEntity.ok(modelService.findModelsByVersion(version));
        } else {
            return ResponseEntity.ok(modelService.getAllModels());
        }
    }

    @PostMapping
    public ResponseEntity<Model> createModel(@RequestBody Model model) {
        return ResponseEntity.status(HttpStatus.CREATED).body(modelService.createModel(model));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Model> updateModel(@PathVariable String id, @RequestBody Model model) {
        if (!modelService.getModelById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        model.setId(id);
        return ResponseEntity.ok(modelService.updateModel(model));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModel(@PathVariable String id) {
        modelService.deleteModel(id);
        return ResponseEntity.noContent().build();
    }
}
