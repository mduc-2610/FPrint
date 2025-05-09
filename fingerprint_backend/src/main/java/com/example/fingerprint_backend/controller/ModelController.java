package com.example.fingerprint_backend.controller;


import com.example.fingerprint_backend.repository.ModelRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class ModelController<T, ID> {
    protected final ModelRepository<T, ID> repository;

    public ModelController(ModelRepository<T, ID> repository) {
        this.repository = repository;
    }

    @GetMapping("")
    public List<T> getAll() { return repository.findAll(); }

}
