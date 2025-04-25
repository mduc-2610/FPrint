package com.example.accesscontrolservice.controller;

import com.example.accesscontrolservice.model.Camera;
import com.example.accesscontrolservice.service.CameraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cameras")
public class CameraController {
    @Autowired
    private CameraService cameraService;

    @GetMapping
    public ResponseEntity<List<Camera>> getAllCameras() {
        return ResponseEntity.ok(cameraService.getAllCameras());
    }

    @GetMapping("/area/{areaId}")
    public ResponseEntity<List<Camera>> getCamerasByAreaId(@PathVariable String areaId) {
        return ResponseEntity.ok(cameraService.getCamerasByAreaId(areaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Camera> getCameraById(@PathVariable String id) {
        return cameraService.getCameraById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Camera> createCamera(@RequestBody Camera camera) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cameraService.createCamera(camera));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Camera> updateCamera(@PathVariable String id, @RequestBody Camera camera) {
        camera.setId(id);
        return ResponseEntity.ok(cameraService.updateCamera(camera));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCamera(@PathVariable String id) {
        cameraService.deleteCamera(id);
        return ResponseEntity.noContent().build();
    }
}
