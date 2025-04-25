package com.example.accesscontrolservice.util;

import com.example.accesscontrolservice.model.AccessLog;
import com.example.accesscontrolservice.model.Area;
import com.example.accesscontrolservice.model.Camera;
import com.example.accesscontrolservice.model.CameraImage;
import com.example.accesscontrolservice.repository.AccessLogRepository;
import com.example.accesscontrolservice.repository.AreaRepository;
import com.example.accesscontrolservice.repository.CameraImageRepository;
import com.example.accesscontrolservice.repository.CameraRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DataGenerator {
    private final AreaRepository areaRepository;
    private final CameraRepository cameraRepository;
    private final AccessLogRepository accessLogRepository;
    private final CameraImageRepository cameraImageRepository;

    private final Faker faker = new Faker();
    private final Random random = new Random();
    private final ObjectMapper objectMapper;

    private static final String reportPath = "../fingerprint_training/reports/";
    
    @Value("${data.generator.enabled:false}")
    private boolean dataGeneratorEnabled;

    @PostConstruct
    public void initializeData() throws IOException {
        if (!dataGeneratorEnabled) {
            System.out.println("Data generator is disabled. Skipping data initialization.");
            return;
        }
        
        accessLogRepository.deleteAll();
        cameraRepository.deleteAll();
        areaRepository.deleteAll();
        cameraImageRepository.deleteAll();

        List<Area> areas = createAreas(10);

        List<Camera> cameras = createCameras(areas);

    }

    private List<Area> createAreas(int count) {
        List<Area> areas = new ArrayList<>();

        // Define real area data
        List<String[]> realAreas = new ArrayList<>();
        realAreas.add(new String[]{"Research and Development Lab", "High-security laboratory for product research and development", "5"});
        realAreas.add(new String[]{"Executive Office Suite", "Executive management offices and conference rooms", "5"});
        realAreas.add(new String[]{"Server Room", "Primary data center and server infrastructure", "5"});
        realAreas.add(new String[]{"Finance Department", "Financial records and accounting offices", "4"});
        realAreas.add(new String[]{"Human Resources", "Personnel files and HR management offices", "4"});
        realAreas.add(new String[]{"Manufacturing Floor", "Main production and assembly area", "3"});
        realAreas.add(new String[]{"Quality Control Lab", "Testing and quality assurance facilities", "3"});
        realAreas.add(new String[]{"Warehouse", "Inventory storage and shipping facilities", "2"});
        realAreas.add(new String[]{"Main Lobby", "Reception area and visitor check-in", "1"});
        realAreas.add(new String[]{"Employee Cafeteria", "Staff dining and break areas", "1"});

        // Create Area objects with real data
        for (int i = 0; i < Math.min(count, realAreas.size()); i++) {
            String[] areaData = realAreas.get(i);
            Area area = Area.builder()
                    .id(UUID.randomUUID().toString())
                    .name(areaData[0])
                    .description(areaData[1])
                    .securityLevel(Integer.parseInt(areaData[2]))
                    .build();
            areas.add(areaRepository.save(area));
        }

        return areas;
    }

    private List<Camera> createCameras(List<Area> areas) {
        List<Camera> cameras = new ArrayList<>();
        for (Area area : areas) {
            int cameraCount = random.nextInt(3) + 1;
            for (int i = 0; i < cameraCount; i++) {
                Camera camera = Camera.builder()
                        .id(UUID.randomUUID().toString())
                        .area(area)
                        .location(faker.address().streetAddress())
                        .status(random.nextBoolean())
                        .installed_at(LocalDateTime.ofInstant(
                                faker.date().past(365, TimeUnit.DAYS).toInstant(),
                                ZoneId.systemDefault()))
                        .build();
                cameras.add(cameraRepository.save(camera));
            }
        }
        return cameras;
    }
}