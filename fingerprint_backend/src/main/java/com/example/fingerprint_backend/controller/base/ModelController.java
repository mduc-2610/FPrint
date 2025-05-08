package com.example.fingerprint_backend.controller.base;


import com.example.fingerprint_backend.repository.base.ModelRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

public abstract class ModelController<T, ID> {
    protected final ModelRepository<T, ID> repository;

    public ModelController(ModelRepository<T, ID> repository) {
        this.repository = repository;
    }

    @GetMapping("")
    public List<T> getAll() { return repository.findAll(); }

}
