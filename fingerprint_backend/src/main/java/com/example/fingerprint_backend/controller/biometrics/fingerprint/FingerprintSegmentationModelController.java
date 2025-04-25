package com.example.fingerprint_backend.controller.biometrics.fingerprint;

import com.example.fingerprint_backend.controller.base.ModelController;
import com.example.fingerprint_backend.model.base.Model;
import com.example.fingerprint_backend.model.biometrics.fingerprint.FingerprintRecognitionModel;
import com.example.fingerprint_backend.model.biometrics.fingerprint.FingerprintSegmentationModel;
import com.example.fingerprint_backend.repository.biometrics.fingerprint.FingerprintSampleRepository;
import com.example.fingerprint_backend.repository.biometrics.fingerprint.FingerprintSegmentationModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fingerprint-segmentation-model")
public class FingerprintSegmentationModelController
        extends ModelController<FingerprintSegmentationModel, String, FingerprintSegmentationModelRepository> {

    @Autowired
    private FingerprintSampleRepository fingerprintSampleRepository;

    public FingerprintSegmentationModelController(FingerprintSegmentationModelRepository repository) {
        super(repository);
    }

    @Override
    protected int calculateTotalUsage(Model model) {
        return fingerprintSampleRepository.countByFingerprintSegmentationModel((FingerprintSegmentationModel) model);
    }

    @Override
    protected float calculateAverageConfidence(Model model) {
        return fingerprintSampleRepository.findAverageQualityByFingerprintSegmentationModelId(
                ((FingerprintSegmentationModel) model).getId());
    }
}
