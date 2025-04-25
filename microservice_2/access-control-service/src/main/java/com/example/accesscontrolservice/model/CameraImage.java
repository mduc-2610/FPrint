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
public class CameraImage {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "camera_id")
    private Camera camera;

    private String image;

    @Lob
    private byte[] imageData;

    private LocalDateTime capturedAt;
}
