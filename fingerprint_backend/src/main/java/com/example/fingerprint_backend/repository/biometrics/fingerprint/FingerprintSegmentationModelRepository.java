package com.example.fingerprint_backend.repository.biometrics.fingerprint;

import com.example.fingerprint_backend.model.biometrics.fingerprint.FingerprintSegmentationModel;
import com.example.fingerprint_backend.repository.base.ModelRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FingerprintSegmentationModelRepository
        extends ModelRepository<FingerprintSegmentationModel, String> {
}
