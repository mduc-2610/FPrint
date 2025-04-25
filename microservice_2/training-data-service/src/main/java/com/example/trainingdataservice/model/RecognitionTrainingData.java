package com.example.trainingdataservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecognitionTrainingData {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "training_data_id")
    private TrainingData trainingData;

    private String fingerprintRecognitionModelId; // Reference to the model in Model Management service
    private String dataPath;
    private int sampleCount;
    private String features;
    private String metaData;
}
