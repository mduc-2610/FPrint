package com.example.trainingdataservice.util;

import com.example.trainingdataservice.model.TrainingData;
import com.example.trainingdataservice.repository.TrainingDataRepository;
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

    private final TrainingDataRepository trainingDataRepository;

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

        trainingDataRepository.deleteAll();

        List<TrainingData> trainingData = loadTrainingDataInfo("training_data_info.json");
    }

    private List<TrainingData> loadTrainingDataInfo(String fileName) throws IOException {
        List<TrainingData> trainingDataList = new ArrayList<>();
        String content = new String(Files.readAllBytes(Paths.get(reportPath, fileName)));
        JSONArray jsonArray = new JSONArray(content);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(0);

            TrainingData trainingData = TrainingData.builder()
                    .name(jsonObject.getString("name"))
                    .purpose(jsonObject.getString("purpose"))
                    .sampleCount(jsonObject.getInt("sampleCount"))
                    .createdAt(LocalDateTime.parse(jsonObject.getString("createdAt"), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .updatedAt(LocalDateTime.parse(jsonObject.getString("updatedAt"), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .source(jsonObject.getString("source"))
                    .resolution(jsonObject.getString("resolution"))
                    .format(jsonObject.getString("format"))
                    .build();
            trainingDataList.add(trainingData);
        }
        trainingDataRepository.saveAll(trainingDataList);

        return trainingDataList;
    }

}