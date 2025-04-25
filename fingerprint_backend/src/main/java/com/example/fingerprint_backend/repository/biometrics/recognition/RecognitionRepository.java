package com.example.fingerprint_backend.repository.biometrics.recognition;

import com.example.fingerprint_backend.model.biometrics.fingerprint.FingerprintRecognitionModel;
import com.example.fingerprint_backend.model.biometrics.recognition.Recognition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecognitionRepository extends JpaRepository<Recognition, String> {
    List<Recognition> findByEmployeeId(String employeeId);
    List<Recognition> findByConfidenceGreaterThan(float confidenceThreshold);
    List<Recognition> findByFingerprintRecognitionModelId(String modelId);
    List<Recognition> findByFingerprintSegmentationModelId(String modelId);
    int countByFingerprintRecognitionModel(FingerprintRecognitionModel model);

    @Query("SELECT COALESCE(AVG(r.confidence), 0) FROM Recognition r WHERE r.fingerprintRecognitionModel = :model")
    float findAverageConfidenceByFingerprintRecognitionModel(@Param("model") FingerprintRecognitionModel model);
}

