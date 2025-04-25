package com.example.accesscontrolservice.service;

import com.example.accesscontrolservice.model.Camera;
import com.example.accesscontrolservice.repository.CameraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CameraService {
    @Autowired
    private CameraRepository cameraRepository;

    public List<Camera> getAllCameras() {
        return cameraRepository.findAll();
    }

    public List<Camera> getCamerasByAreaId(String areaId) {
        return cameraRepository.findByAreaId(areaId);
    }

    public Optional<Camera> getCameraById(String id) {
        return cameraRepository.findById(id);
    }

    public Camera createCamera(Camera camera) {
        return cameraRepository.save(camera);
    }

    public Camera updateCamera(Camera camera) {
        return cameraRepository.save(camera);
    }

    public void deleteCamera(String id) {
        cameraRepository.deleteById(id);
    }
}
