package com.example.accesscontrolservice.repository;

import com.example.accesscontrolservice.model.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CameraRepository extends JpaRepository<Camera, String> {
    List<Camera> findByAreaId(String areaId);
}
