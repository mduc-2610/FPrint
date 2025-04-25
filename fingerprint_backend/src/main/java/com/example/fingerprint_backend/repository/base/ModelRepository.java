package com.example.fingerprint_backend.repository.base;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface ModelRepository<T, ID> extends JpaRepository<T, ID> {
    Optional<T> findByName(String name);
    List<T> findByVersion(String version);
    List<T> findByAccuracyGreaterThanEqual(float minAccuracy);
    Optional<T> findTopByOrderByCreatedAtDesc();
    default Optional<T> findLatestModel() {
        return findTopByOrderByCreatedAtDesc();
    }

}
