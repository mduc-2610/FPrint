package com.example.fingerprint_backend.controller.biometrics.fingerprint;

import com.example.fingerprint_backend.controller.base.ModelController;
import com.example.fingerprint_backend.model.base.Model;
import com.example.fingerprint_backend.model.biometrics.fingerprint.FingerprintSegmentationModel;
import com.example.fingerprint_backend.repository.base.ModelRepository;
import com.example.fingerprint_backend.repository.biometrics.fingerprint.FingerprintSegmentationModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
