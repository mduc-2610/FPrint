package com.example.fingerprint_backend.model.access;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Area {
    @Id
    private String id;
    private String name;
    private int securityLevel;
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "area", fetch = FetchType.LAZY)
    private List<AccessLog> accessLogs;
}
