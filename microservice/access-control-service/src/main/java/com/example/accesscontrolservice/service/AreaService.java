package com.example.accesscontrolservice.service;

import com.example.accesscontrolservice.model.Area;
import com.example.accesscontrolservice.repository.AreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AreaService {
    @Autowired
    private AreaRepository areaRepository;

    public List<Area> getAllAreas() {
        return areaRepository.findAll();
    }

    public Optional<Area> getAreaById(String id) {
        return areaRepository.findById(id);
    }

    public Area createArea(Area area) {
        return areaRepository.save(area);
    }

    public Area updateArea(Area area) {
        return areaRepository.save(area);
    }

    public void deleteArea(String id) {
        areaRepository.deleteById(id);
    }
}