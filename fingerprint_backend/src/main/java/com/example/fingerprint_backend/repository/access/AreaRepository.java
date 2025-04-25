package com.example.fingerprint_backend.repository.access;

import com.example.fingerprint_backend.model.access.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaRepository extends JpaRepository<Area, String> {
}
