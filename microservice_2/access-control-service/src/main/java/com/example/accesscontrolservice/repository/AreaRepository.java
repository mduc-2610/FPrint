package com.example.accesscontrolservice.repository;

import com.example.accesscontrolservice.model.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaRepository extends JpaRepository<Area, String> {
}
