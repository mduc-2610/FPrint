package com.example.accesscontrolservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    private List<Camera> cameras;
    
    @JsonIgnore
    @OneToMany(mappedBy = "area", fetch = FetchType.LAZY)
    private List<AccessLog> accessLogs;
}