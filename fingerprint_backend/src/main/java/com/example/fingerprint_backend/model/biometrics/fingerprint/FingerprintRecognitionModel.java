package com.example.fingerprint_backend.model.biometrics.fingerprint;

import com.example.fingerprint_backend.model.base.Model;
import com.example.fingerprint_backend.model.biometrics.recognition.Recognition;
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
public class FingerprintRecognitionModel extends Model {

    @JsonIgnore
    @OneToMany(mappedBy = "fingerprintRecognitionModel", fetch = FetchType.LAZY)
    private List<Recognition> recognitions;

}
