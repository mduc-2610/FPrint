package com.example.fingerprint_backend.controller.auth;

import com.example.fingerprint_backend.model.auth.Admin;
import com.example.fingerprint_backend.repository.auth.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminRepository adminRepository;

    @GetMapping
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable String id) {
        Optional<Admin> admin = adminRepository.findById(id);
        return admin.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Admin createAdmin(@RequestBody Admin admin) {
        return adminRepository.save(admin);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Admin> updateAdmin(@PathVariable String id, @RequestBody Admin admin) {
        if (!adminRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        admin.setId(id);
        return ResponseEntity.ok(adminRepository.save(admin));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable String id) {
        if (!adminRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        adminRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}