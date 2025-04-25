package com.example.fingerprint_backend.repository.biometrics.fingerprint;

import com.example.fingerprint_backend.model.biometrics.fingerprint.FingerprintSegmentationModel;
import com.example.fingerprint_backend.repository.base.ModelRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


import com.example.fingerprint_backend.model.biometrics.fingerprint.FingerprintSegmentationModel;
import org.springframework.stereotype.Repository;

@Repository
public interface FingerprintSegmentationModelRepository
        extends ModelRepository<FingerprintSegmentationModel, String> {
}
