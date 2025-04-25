package com.example.accesscontrolservice.controller;

import com.example.accesscontrolservice.model.Area;
import com.example.accesscontrolservice.repository.AreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/area")
public class AreaController {
    @Autowired
    private AreaRepository areaRepository;

    @GetMapping
    public ResponseEntity<List<Area>> getAllAreas() {
        return ResponseEntity.ok(areaRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Area> getAreaById(@PathVariable String id) {
        return areaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Area> createArea(@RequestBody Area area) {
        return ResponseEntity.status(HttpStatus.CREATED).body(areaRepository.save(area));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Area> updateArea(@PathVariable String id, @RequestBody Area area) {
        if (!areaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        area.setId(id);
        return ResponseEntity.ok(areaRepository.save(area));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArea(@PathVariable String id) {
        areaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}