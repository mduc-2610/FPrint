package com.example.fingerprint_backend.repository.biometrics.fingerprint;

import com.example.fingerprint_backend.model.biometrics.fingerprint.FingerprintSample;
import com.example.fingerprint_backend.model.biometrics.fingerprint.FingerprintSegmentationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FingerprintSampleRepository extends JpaRepository<FingerprintSample, String> {
    // Basic queries by employee
    List<FingerprintSample> findByEmployeeId(String employeeId);

    boolean existsByIdAndEmployeeId(String id, String employeeId);

    // Active status queries
    List<FingerprintSample> findByEmployeeIdAndActiveTrue(String employeeId);

    List<FingerprintSample> findByEmployeeIdAndActiveFalse(String employeeId);

    int countByEmployeeIdAndActiveTrue(String employeeId);

    // Position related queries
    List<FingerprintSample> findByEmployeeIdAndPositionAndActiveTrue(String employeeId, String position);

    List<FingerprintSample> findByEmployeeIdAndPositionAndActiveFalse(String employeeId, String position);

    // Model related queries
    List<FingerprintSample> findByFingerprintSegmentationModelId(String modelId);

    List<FingerprintSample> findByFingerprintRecognitionModelId(String modelId);

    int countByFingerprintSegmentationModel(FingerprintSegmentationModel model);

    // Quality analysis
    @Query("SELECT AVG(f.quality) FROM FingerprintSample f WHERE f.fingerprintSegmentationModel.id = :modelId")
    float findAverageQualityByFingerprintSegmentationModelId(@Param("modelId") String modelId);

    // Update operations
    @Modifying
    @Transactional
    @Query("UPDATE FingerprintSample f SET f.active = :active WHERE f.id = :id")
    int updateActiveStatus(@Param("id") String id, @Param("active") boolean active);

    @Modifying
    @Transactional
    @Query("UPDATE FingerprintSample f SET f.active = false WHERE f.employee.id = :employeeId")
    int deactivateAllForEmployee(@Param("employeeId") String employeeId);
}