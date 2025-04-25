package com.example.fingerprint_backend.model.auth;

import com.example.fingerprint_backend.model.base.User;
import com.example.fingerprint_backend.model.biometrics.fingerprint.FingerprintSample;
import com.example.fingerprint_backend.model.biometrics.recognition.Recognition;
import com.example.fingerprint_backend.model.access.AccessLog;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class Employee extends User {

    public Employee(String id, String fullName, String phoneNumber, String photo, String address, @Nullable String username, @Nullable String email, @Nullable String password) {
        super(id, fullName, phoneNumber, photo, address, username, email, password);
        this.maxNumberSamples = 5;
    }

    public Employee(String id, String fullName, String phoneNumber, String photo, String address, int maxNumberSamples) {
        super(id, fullName, phoneNumber, photo, address, null, null, null);
        this.maxNumberSamples = maxNumberSamples;
    }

    @JsonIgnore
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AccessLog> accessLogs;

    @JsonIgnore
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Recognition> recognitions;

    @JsonIgnore
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FingerprintSample> fingerprintSamples;

    private int maxNumberSamples;
}