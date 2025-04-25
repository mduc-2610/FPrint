package com.example.fingerprint_backend.repository.auth;

import com.example.fingerprint_backend.model.auth.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, String> {
}