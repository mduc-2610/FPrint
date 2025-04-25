package com.example.accesscontrolservice.controller;

import com.example.accesscontrolservice.model.Area;
import com.example.accesscontrolservice.service.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/areas")
public class AreaController {
    @Autowired
    private AreaService areaService;

    @GetMapping
    public ResponseEntity<List<Area>> getAllAreas() {
        return ResponseEntity.ok(areaService.getAllAreas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Area> getAreaById(@PathVariable String id) {
        return areaService.getAreaById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Area> createArea(@RequestBody Area area) {
        return ResponseEntity.status(HttpStatus.CREATED).body(areaService.createArea(area));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Area> updateArea(@PathVariable String id, @RequestBody Area area) {
        area.setId(id);
        return ResponseEntity.ok(areaService.updateArea(area));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArea(@PathVariable String id) {
        areaService.deleteArea(id);
        return ResponseEntity.noContent().build();
    }
}
