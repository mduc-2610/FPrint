package com.example.modelmanagementservice.repository;

import com.example.modelmanagementservice.model.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRepository extends JpaRepository<Model, String> {
    List<Model> findByNameContaining(String name);
    List<Model> findByVersion(String version);
}
