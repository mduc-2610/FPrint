package com.example.fingerprint_backend.repository;

import com.example.fingerprint_backend.model.FingerprintSegmentationModel;
import org.springframework.stereotype.Repository;

@Repository
public interface FingerprintSegmentationModelRepository
        extends ModelRepository<FingerprintSegmentationModel, String> {
}
