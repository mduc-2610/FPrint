package com.example.fingerprint_backend.repository.base;

import com.example.fingerprint_backend.model.base.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
}