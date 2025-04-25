package com.example.usermanagementservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class Employee extends User {

    public Employee(String id, String fullName, String phoneNumber, String photo, String address, @Nullable String username, @Nullable String email, @Nullable String password) {
        super(id, fullName, phoneNumber, photo, address, username, email, password);
    }

    public Employee(String id, String fullName, String phoneNumber, String photo, String address) {
        super(id, fullName, phoneNumber, photo, address, null, null, null);
    }
}