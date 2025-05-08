package com.example.fingerprint_backend.repository.biometrics.fingerprint;

import com.example.fingerprint_backend.model.biometrics.fingerprint.FingerprintRecognitionModel;
import com.example.fingerprint_backend.repository.base.ModelRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FingerprintRecognitionModelRepository
        extends ModelRepository<FingerprintRecognitionModel, String> {
}
