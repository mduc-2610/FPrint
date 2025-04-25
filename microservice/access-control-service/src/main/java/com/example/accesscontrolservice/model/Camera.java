package com.example.accesscontrolservice.model;

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
public class Camera {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;

    private String location;
    private boolean status;
    private LocalDateTime installed_at;

    @OneToMany(mappedBy = "camera")
    private List<CameraImage> cameraImages;
}
