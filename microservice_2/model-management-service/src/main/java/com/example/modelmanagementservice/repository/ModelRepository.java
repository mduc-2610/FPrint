package com.example.modelmanagementservice.repository;

import com.example.modelmanagementservice.model.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.NoRepositoryBean;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ModelRepository<T extends Model> extends JpaRepository<T, String> {
    List<T> findByAccuracyGreaterThan(float accuracy);
    List<T> findByCreatedAtAfter(LocalDateTime date);
    List<T> findByNameContainingIgnoreCase(String name);
    List<T> findTop5ByOrderByCreatedAtDesc();
    List<T> findByVersion(String version);
}