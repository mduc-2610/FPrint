package com.example.usermanagementservice.service;

import com.example.usermanagementservice.model.Admin;
import com.example.usermanagementservice.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserService userService;

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Optional<Admin> getAdminById(String id) {
        return adminRepository.findById(id);
    }

    public Admin createAdmin(Admin admin) {
        return adminRepository.save((Admin) userService.createUser(admin));
    }

    public Admin updateAdmin(Admin admin) {
        return adminRepository.save((Admin) userService.updateUser(admin));
    }

    public void deleteAdmin(String id) {
        adminRepository.deleteById(id);
    }
}
