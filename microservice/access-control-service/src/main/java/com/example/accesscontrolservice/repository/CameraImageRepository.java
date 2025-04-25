package com.example.accesscontrolservice.repository;

import com.example.accesscontrolservice.model.CameraImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CameraImageRepository extends JpaRepository<CameraImage, String> {
    List<CameraImage> findByCameraId(String cameraId);
}
