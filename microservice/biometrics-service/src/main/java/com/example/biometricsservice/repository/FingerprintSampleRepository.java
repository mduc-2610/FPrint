package com.example.biometricsservice.repository;

import com.example.biometricsservice.model.FingerprintSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FingerprintSampleRepository extends JpaRepository<FingerprintSample, String> {
    List<FingerprintSample> findByEmployeeId(String employeeId);
    List<FingerprintSample> findByEmployeeIdAndPosition(String employeeId, String position);
}