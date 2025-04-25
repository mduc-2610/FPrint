package com.example.usermanagementservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User {
    @Id
    private String id;
    private String fullName;
    private String phoneNumber;
    private String photo;
    private String address;
    private String username;
    private String email;
    private String password;
}
