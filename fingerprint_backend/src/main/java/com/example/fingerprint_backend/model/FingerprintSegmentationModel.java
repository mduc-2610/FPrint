package com.example.fingerprint_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.FetchType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FingerprintSegmentationModel extends Model {

    @JsonIgnore
    @OneToMany(mappedBy = "fingerprintSegmentationModel", fetch = FetchType.LAZY)
    private List<Recognition> recognitions;

}
