package com.example.fingerprint_backend.repository.base;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface ModelRepository<T, ID> extends JpaRepository<T, ID> {
    Optional<T> findTopByOrderByCreatedAtDesc();

}
