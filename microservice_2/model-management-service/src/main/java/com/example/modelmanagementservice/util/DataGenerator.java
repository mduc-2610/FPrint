package com.example.modelmanagementservice.util;

import com.example.modelmanagementservice.model.FingerprintRecognitionModel;
import com.example.modelmanagementservice.model.FingerprintSegmentationModel;
import com.example.modelmanagementservice.repository.FingerprintRecognitionModelRepository;
import com.example.modelmanagementservice.repository.FingerprintSegmentationModelRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
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

    private final FingerprintRecognitionModelRepository fingerprintRecognitionModelRepository;
    private final FingerprintSegmentationModelRepository fingerprintSegmentationModelRepository;

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

        fingerprintRecognitionModelRepository.deleteAll();
        fingerprintSegmentationModelRepository.deleteAll();

        List<FingerprintRecognitionModel> recognitionModel = loadRecognitionModel("fingerprint_recognition_models.json");
        List<FingerprintSegmentationModel> segmentationModel = loadSegmentationModel("fingerprint_segmentation_models.json");

    }

    private List<FingerprintRecognitionModel> loadRecognitionModel(String fileName) throws IOException {
        List<FingerprintRecognitionModel> models = new ArrayList<>();
        String content = new String(Files.readAllBytes(Paths.get(reportPath, fileName)));
        JSONArray jsonArray = new JSONArray(content);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            FingerprintRecognitionModel model = FingerprintRecognitionModel.builder()
                    .name(jsonObject.getString("name"))
                    .pathName(jsonObject.getString("path_name"))
                    .accuracy((float) jsonObject.getDouble("accuracy"))
                    .valAccuracy((float) jsonObject.getDouble("valAccuracy"))
                    .version(jsonObject.getString("version"))
                    .createdAt(LocalDateTime.parse(jsonObject.getString("createdAt"), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .updatedAt(LocalDateTime.parse(jsonObject.getString("updatedAt"), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
            models.add(model);
        }

        fingerprintRecognitionModelRepository.saveAll(models);

        return models;
    }

    private List<FingerprintSegmentationModel> loadSegmentationModel(String fileName) throws IOException {
        List<FingerprintSegmentationModel> models = new ArrayList<>();
        String content = new String(Files.readAllBytes(Paths.get(reportPath, fileName)));
        JSONArray jsonArray = new JSONArray(content);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            FingerprintSegmentationModel model = FingerprintSegmentationModel.builder()
                    .name(jsonObject.getString("name"))
                    .pathName(jsonObject.getString("path_name"))
                    .accuracy((float) jsonObject.getDouble("accuracy"))
                    .valAccuracy((float) jsonObject.getDouble("valAccuracy"))
                    .version(jsonObject.getString("version"))
                    .createdAt(LocalDateTime.parse(jsonObject.getString("createdAt"), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .updatedAt(LocalDateTime.parse(jsonObject.getString("updatedAt"), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
            models.add(model);
        }
        fingerprintSegmentationModelRepository.saveAll(models);

        return models;
    }

}