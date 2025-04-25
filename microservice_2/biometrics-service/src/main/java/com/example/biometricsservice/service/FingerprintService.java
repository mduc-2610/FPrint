package com.example.biometricsservice.service;

import com.example.biometricsservice.client.AccessControlClient;
import com.example.biometricsservice.client.ModelManagementClient;
import com.example.biometricsservice.client.UserManagementClient;
import com.example.biometricsservice.model.FingerprintSample;
import com.example.biometricsservice.model.Recognition;
import com.example.biometricsservice.model.RecognitionResult;
import com.example.biometricsservice.repository.FingerprintSampleRepository;
import com.example.biometricsservice.repository.RecognitionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FingerprintService {

    // @Value("${python.venv.path}")
    private String VENV_PYTHON = "../fingerprint_training/env/Scripts/python.exe";

    // @Value("${python.script.dir}")
    private String SCRIPT_DIR = "../fingerprint_training/reports";
    
    // @Value("${python.script.path}")
    private String PYTHON_SCRIPT_PATH = "../fingerprint_training/fingerprint_recognition.py";
    
    // @Value("${fingerprint.dataset.path}")
    private String DATASET_BASE_PATH = "../fingerprint_training/fingerprint_adapting_dataset/";

    private static final String RECOGNITION_RESULT_FILE = "recognition_result.json";

    private final FingerprintSampleRepository fingerprintSampleRepository;
    private final RecognitionRepository recognitionRepository;
    private final UserManagementClient userManagementClient;
    private final AccessControlClient accessControlClient;
    private final ModelManagementClient modelManagementClient;
    private final Logger logger = LoggerFactory.getLogger(FingerprintService.class);
    
    @Transactional
    public FingerprintSample registerFingerprint(
            String employeeId,
            MultipartFile file,
            String position,
            String segmentationModelId,
            String recognitionModelId) throws Exception {

        logger.info("Starting fingerprint registration for employeeId: {}", employeeId);

        // var employeeResponse = userManagementClient.getEmployeeById(employeeId);
        // if (employeeResponse.getStatusCode().is4xxClientError()) {
        //     logger.error("Employee not found: {}", employeeId);
        //     throw new Exception("Employee with ID " + employeeId + " not found");
        // }
        // logger.info("Employee details fetched successfully for ID: {}", employeeId);

        var segModelResponse = modelManagementClient.getSegmentationModelById(segmentationModelId);
        if (segModelResponse.getStatusCode().is4xxClientError()) {
            logger.error("Segmentation model not found: {}", segmentationModelId);
            throw new Exception("Segmentation model with ID " + segmentationModelId + " not found");
        }
        logger.info("Segmentation model fetched: {}", segmentationModelId);

        var recModelResponse = modelManagementClient.getRecognitionModelById(recognitionModelId);
        if (recModelResponse.getStatusCode().is4xxClientError()) {
            logger.error("Recognition model not found: {}", recognitionModelId);
            throw new Exception("Recognition model with ID " + recognitionModelId + " not found");
        }
        logger.info("Recognition model fetched: {}", recognitionModelId);

        String segModelJson = parseResponseToJson(segModelResponse.getBody());
        String recModelJson = parseResponseToJson(recModelResponse.getBody());

        JsonNode segModelNode = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readTree(segModelJson);
        JsonNode recModelNode = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readTree(recModelJson);

        String segmentationModelPath = segModelNode.get("pathName").asText();
        String recognitionModelPath = recModelNode.get("pathName").asText();
        logger.debug("Segmentation model path: {}", segmentationModelPath);
        logger.debug("Recognition model path: {}", recognitionModelPath);

        String employeeDir = DATASET_BASE_PATH + "/" + employeeId;
        Path employeePath = Paths.get(employeeDir);

        try {
            Files.createDirectories(employeePath);
            logger.info("Created directory for employee at: {}", employeePath);
        } catch (IOException e) {
            logger.error("Failed to create directory for employee {}: {}", employeeId, e.getMessage(), e);
            throw new Exception("Failed to create directory for employee " + employeeId, e);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".bmp";

        String filename = employeeId + "_" + position + extension;
        Path filePath = Paths.get(employeeDir, filename);

        try {
            byte[] fileBytes = file.getBytes();
            Files.write(filePath, fileBytes);
            logger.info("Fingerprint image saved at: {}", filePath);

            FingerprintSample sample = FingerprintSample.builder()
                    .employeeId(employeeId)
                    .image(filename)
                    .imageData(fileBytes)
                    .position(position)
                    .capturedAt(LocalDateTime.now())
                    .segmentationModelId(segmentationModelId)
                    .recognitionModelId(recognitionModelId)
                    .quality(1.0)
                    .build();

            FingerprintSample savedSample = fingerprintSampleRepository.save(sample);
            logger.info("Fingerprint sample saved to database with ID: {}", savedSample.getId());

            updateFingerprintModel(segmentationModelPath, recognitionModelPath);
            logger.info("Fingerprint models updated successfully");

            return savedSample;

        } catch (IOException e) {
            logger.error("Failed to save fingerprint image: {}", e.getMessage(), e);
            throw new Exception("Failed to save fingerprint image: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during fingerprint registration: {}", e.getMessage(), e);
            throw e;
        }
    }

    public RecognitionResult recognizeFingerprint(
        MultipartFile fingerprintImage,
        String segmentationModelId,
        String recognitionModelId) throws Exception {

        if (fingerprintImage == null || fingerprintImage.isEmpty()) {
            throw new IllegalArgumentException("Fingerprint image cannot be empty");
        }

        var segModelResponse = modelManagementClient.getSegmentationModelById(segmentationModelId);
        if (segModelResponse.getStatusCode().is4xxClientError()) {
            throw new IllegalArgumentException("Segmentation model with ID " + segmentationModelId + " not found");
        }

        var recModelResponse = modelManagementClient.getRecognitionModelById(recognitionModelId);
        if (recModelResponse.getStatusCode().is4xxClientError()) {
            throw new IllegalArgumentException("Recognition model with ID " + recognitionModelId + " not found");
        }

        String segModelJson = parseResponseToJson(segModelResponse.getBody());
        String recModelJson = parseResponseToJson(recModelResponse.getBody());

        JsonNode segModelNode = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readTree(segModelJson);
        JsonNode recModelNode = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readTree(recModelJson);

        String segmentationModelPath = segModelNode.path("pathName").asText();
        String recognitionModelPath = recModelNode.path("pathName").asText();

        Path tempPath = Files.createTempFile("fingerprint_", ".bmp");
        try {
            Files.write(tempPath, fingerprintImage.getBytes());
            logger.info("Fingerprint saved temporarily to: {}", tempPath.toAbsolutePath());

            executeRecognitionScript(tempPath.toString(), segmentationModelPath, recognitionModelPath);

            return readRecognitionResultFromFile();
        } catch (Exception e) {
            logger.error("Fingerprint recognition failed", e);
            throw new RuntimeException("Failed to recognize fingerprint", e);
        } finally {
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException e) {
                logger.warn("Failed to delete temporary file", e);
            }
        }
    }

    private String parseResponseToJson(Object responseBody) {
        return responseBody.toString()
            .replace("{", "{\"")
            .replace("=", "\":\"")
            .replace(", ", "\", \"")
            .replace("}", "\"}");
    }

    @Transactional
    public Object createAccessLog(
            String employeeId,
            Object area,
            String accessType,
            boolean isMatched,
            double confidence,
            String segmentationModelId,
            String recognitionModelId) throws Exception {

        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> accessLogData = new HashMap<>();
        accessLogData.put("area", area);
        accessLogData.put("employeeId", employeeId);
        accessLogData.put("timestamp", now);
        accessLogData.put("accessType", accessType);
        accessLogData.put("authorized", isMatched);
        logger.info("Creating access log with areaId: {}", area);

        var accessLogResponse = accessControlClient.createAccessLog(accessLogData);

        if (accessLogResponse.getStatusCode().is4xxClientError()) {
            throw new Exception("Failed to create access log");
        }
        
        Map<String, Object> responseBody = accessLogResponse.getBody();
        String accessLogId = responseBody.get("id").toString();
        
        Recognition recognition = Recognition.builder()
            .employeeId(employeeId)
            .accessLogId(accessLogId)
            .fingerprintSegmentationModelId(segmentationModelId)
            .fingerprintRecognitionModelId(recognitionModelId)
            .timestamp(now)
            .confidence((float) confidence)
            .build();
        
        recognitionRepository.save(recognition);
        accessControlClient.updateRecognitionId(accessLogId, recognition.getId());


        return accessLogResponse.getBody();
    }

    private void updateFingerprintModel(String segmentationModelPath, String recognitionModelPath)
            throws IOException, InterruptedException {

        ProcessBuilder processBuilder = new ProcessBuilder(
                VENV_PYTHON,
                PYTHON_SCRIPT_PATH,
                "--update-model",
                "--seg-path-name", segmentationModelPath,
                "--rec-path-name", recognitionModelPath
        );

        Process process = processBuilder.start();
        String errorOutput = new String(process.getErrorStream().readAllBytes());

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("Python script execution failed with exit code: "
                    + exitCode + ", Error: " + errorOutput);
        }
    }

    private String executeRecognitionScript(String imagePath, String segmentationModelPath, String recognitionModelPath)
            throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                VENV_PYTHON,
                PYTHON_SCRIPT_PATH,
                "--recognize",
                imagePath,
                "--seg-path-name", segmentationModelPath,
                "--rec-path-name", recognitionModelPath
        );

        Process process = processBuilder.start();

        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            String errorOutput = new String(process.getErrorStream().readAllBytes());
            throw new IOException("Python script execution failed: " + errorOutput);
        }

        return output;
    }

    private RecognitionResult readRecognitionResultFromFile() throws IOException {
        File resultFile = Paths.get(SCRIPT_DIR, RECOGNITION_RESULT_FILE).toFile();

        if (!resultFile.exists()) {
            throw new IOException("Recognition result file not found");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(resultFile);

        if (rootNode.has("error")) {
            throw new IOException("Recognition error: " + rootNode.get("error").asText());
        }

        JsonNode similarityNode = rootNode.get("similarity");

        String employeeId = null;
        double confidence = 0.0;

        if (similarityNode != null) {
            JsonNode employeeIdNode = similarityNode.get("employee_id");
            if (employeeIdNode != null && !employeeIdNode.isNull()) {
                employeeId = employeeIdNode.asText();
            }

            confidence = similarityNode.get("confidence").asDouble();
        }

        System.out.println("Successfully parsed result from file: " +
                "employeeId=" + employeeId +
                ", confidence=" + confidence);

        return new RecognitionResult(employeeId, confidence);
    }
}



// @Transactional
// public List<FingerprintSample> registerFingerprints(
//         String employeeId,
//         List<MultipartFile> files,
//         List<String> positions,
//         String segmentationModelId,
//         String recognitionModelId) throws Exception {

//     logger.info("Starting bulk fingerprint registration for employeeId: {}", employeeId);

//     var employeeResponse = userManagementClient.getEmployeeById(employeeId);
//     if (employeeResponse.getStatusCode().is4xxClientError()) {
//         logger.error("Employee not found: {}", employeeId);
//         throw new Exception("Employee with ID " + employeeId + " not found");
//     }
//     logger.info("Employee validated: {}", employeeId);

//     var segModelResponse = modelManagementClient.getSegmentationModelById(segmentationModelId);
//     if (segModelResponse.getStatusCode().is4xxClientError()) {
//         logger.error("Segmentation model not found: {}", segmentationModelId);
//         throw new Exception("Segmentation model with ID " + segmentationModelId + " not found");
//     }

//     var recModelResponse = modelManagementClient.getRecognitionModelById(recognitionModelId);
//     if (recModelResponse.getStatusCode().is4xxClientError()) {
//         logger.error("Recognition model not found: {}", recognitionModelId);
//         throw new Exception("Recognition model with ID " + recognitionModelId + " not found");
//     }

    // String segModelJson = parseResponseToJson(segModelResponse.getBody());
    // String recModelJson = parseResponseToJson(recModelResponse.getBody());

    // JsonNode segModelNode = new ObjectMapper()
    //     .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    //     .readTree(segModelJson);
    // JsonNode recModelNode = new ObjectMapper()
    //     .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    //     .readTree(recModelJson);

//     logger.debug("Parsing segmentation model JSON: {}", segModelJson);
//     logger.debug("Parsing recognition model JSON: {}", recModelJson);

//     String segmentationModelPath = segModelNode.get("pathName").asText();
//     String recognitionModelPath = recModelNode.get("pathName").asText();

//     logger.info("Segmentation model path: {}", segmentationModelPath);
//     logger.info("Recognition model path: {}", recognitionModelPath);

//     String employeeDir = DATASET_BASE_PATH + "/" + employeeId;
//     Path employeePath = Paths.get(employeeDir);

//     try {
//         Files.createDirectories(employeePath);
//         logger.info("Created directory for employee: {}", employeePath);
//     } catch (IOException e) {
//         logger.error("Failed to create directory for employee {}: {}", employeeId, e.getMessage(), e);
//         throw new Exception("Failed to create directory for employee " + employeeId, e);
//     }

//     List<FingerprintSample> samples = new ArrayList<>();

//     for (int i = 0; i < files.size(); i++) {
//         MultipartFile file = files.get(i);
//         String position = positions.get(i);

//         String originalFilename = file.getOriginalFilename();
//         String extension = originalFilename != null ?
//                 originalFilename.substring(originalFilename.lastIndexOf(".")) : ".bmp";

//         String filename = employeeId + "_" + position + extension;
//         Path filePath = Paths.get(employeeDir, filename);

//         try {
//             byte[] fileBytes = file.getBytes();
//             Files.write(filePath, fileBytes);
//             logger.info("Saved fingerprint {} for position {} at {}", filename, position, filePath);

//             FingerprintSample sample = FingerprintSample.builder()
//                     .employeeId(employeeId)
//                     .image(filename)
//                     .imageData(fileBytes)
//                     .position(position)
//                     .capturedAt(LocalDateTime.now())
//                     .quality(1.0)
//                     .build();

//             FingerprintSample savedSample = fingerprintSampleRepository.save(sample);
//             logger.info("Saved fingerprint sample to DB with ID: {}", savedSample.getId());

//             samples.add(savedSample);

//         } catch (IOException e) {
//             logger.error("Failed to write fingerprint image {}: {}", filename, e.getMessage(), e);
//             throw new Exception("Failed to save fingerprint image: " + e.getMessage(), e);
//         } catch (Exception e) {
//             logger.error("Unexpected error while saving fingerprint {}: {}", filename, e.getMessage(), e);
//             throw e;
//         }
//     }

//     try {
//         updateFingerprintModel(segmentationModelPath, recognitionModelPath);
//         logger.info("Fingerprint models updated successfully");
//     } catch (Exception e) {
//         logger.error("Model update failed: {}", e.getMessage(), e);
//         throw new Exception("Failed to update fingerprint models", e);
//     }

//     return samples;
// }

