package com.example.fingerprint_backend.controller;

import com.example.fingerprint_backend.model.FingerprintSegmentationModel;
import com.example.fingerprint_backend.repository.ModelRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fingerprint-segmentation-model")
public class FingerprintSegmentationModelController
        extends ModelController<FingerprintSegmentationModel, String> {

    public FingerprintSegmentationModelController(ModelRepository<FingerprintSegmentationModel, String> repository) {
        super(repository);
    }
}
