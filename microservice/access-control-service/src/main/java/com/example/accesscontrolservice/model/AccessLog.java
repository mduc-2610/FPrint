package com.example.accesscontrolservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;

    private String employeeId;  // Reference to the employee in the User Management service
    private LocalDateTime timestamp;
    private boolean authorized;
    private String accessType;
    private String recognitionId; // Reference to the recognition in the Biometrics service
}
