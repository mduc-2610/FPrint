package com.example.modelmanagementservice.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Entity;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class FingerprintRecognitionModel extends Model {
}

