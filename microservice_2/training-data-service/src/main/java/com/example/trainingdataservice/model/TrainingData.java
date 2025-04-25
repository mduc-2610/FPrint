package com.example.trainingdataservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingData {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String purpose;
    private int sampleCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String source;
    private String resolution;
    private String format;

    @OneToMany(mappedBy = "trainingData", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SegmentationTrainingData> segmentationTrainingData;

    @OneToMany(mappedBy = "trainingData", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RecognitionTrainingData> recognitionTrainingData;
}
