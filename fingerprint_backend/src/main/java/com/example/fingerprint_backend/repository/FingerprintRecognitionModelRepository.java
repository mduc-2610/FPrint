package com.example.fingerprint_backend.repository;

import com.example.fingerprint_backend.model.FingerprintRecognitionModel;
import org.springframework.stereotype.Repository;

@Repository
public interface FingerprintRecognitionModelRepository
        extends ModelRepository<FingerprintRecognitionModel, String> {
}
