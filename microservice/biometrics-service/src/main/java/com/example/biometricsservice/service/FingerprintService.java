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

    @Value("${python.venv.path}")
    private String VENV_PYTHON;

    @Value("${python.script.dir}")
    private String SCRIPT_DIR;

    @Value("${python.script.path}")
    private String PYTHON_SCRIPT_PATH;

    @Value("${fingerprint.dataset.path}")
    private String DATASET_BASE_PATH;

    private static final String RECOGNITION_RESULT_FILE = "recognition_result.json";

    private final FingerprintSampleRepository fingerprintSampleRepository;
    private final RecognitionRepository recognitionRepository;
    private final UserManagementClient userManagementClient;
    private final AccessControlClient accessControlClient;
    private final ModelManagementClient modelManagementClient;

    @Transactional
    public FingerprintSample registerFingerprint(
            String employeeId,
            MultipartFile file,
            String position,
            String segmentationModelId,
            String recognitionModelId) throws Exception {

        // Verify employee exists
        var employeeResponse = userManagementClient.getEmployeeById(employeeId);
        if (employeeResponse.getStatusCode().is4xxClientError()) {
            throw new Exception("Employee with ID " + employeeId + " not found");
        }

        // Verify models exist
        var segModelResponse = modelManagementClient.getSegmentationModelById(segmentationModelId);
        if (segModelResponse.getStatusCode().is4xxClientError()) {
            throw new Exception("Segmentation model with ID " + segmentationModelId + " not found");
        }

        var recModelResponse = modelManagementClient.getRecognitionModelById(recognitionModelId);
        if (recModelResponse.getStatusCode().is4xxClientError()) {
            throw new Exception("Recognition model with ID " + recognitionModelId + " not found");
        }

        // Extract model paths from response
        JsonNode segModelNode = new ObjectMapper().readTree(segModelResponse.getBody().toString());
        JsonNode recModelNode = new ObjectMapper().readTree(recModelResponse.getBody().toString());

        String segmentationModelPath = segModelNode.get("pathName").asText();
        String recognitionModelPath = recModelNode.get("pathName").asText();

        String employeeDir = DATASET_BASE_PATH + "/" + employeeId;
        Path employeePath = Paths.get(employeeDir);

        try {
            Files.createDirectories(employeePath);
        } catch (IOException e) {
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

            FingerprintSample sample = FingerprintSample.builder()
                    .employeeId(employeeId)
                    .image(filename)
                    .imageData(fileBytes)
                    .position(position)
                    .capturedAt(LocalDateTime.now())
                    .quality(1.0)
                    .build();

            FingerprintSample savedSample = fingerprintSampleRepository.save(sample);

            updateFingerprintModel(segmentationModelPath, recognitionModelPath);

            return savedSample;

        } catch (IOException e) {
            throw new Exception("Failed to save fingerprint image: " + e.getMessage(), e);
        }
    }

    @Transactional
    public List<FingerprintSample> registerFingerprints(
            String employeeId,
            List<MultipartFile> files,
            List<String> positions,
            String segmentationModelId,
            String recognitionModelId) throws Exception {

        // Verify employee exists
        var employeeResponse = userManagementClient.getEmployeeById(employeeId);
        if (employeeResponse.getStatusCode().is4xxClientError()) {
            throw new Exception("Employee with ID " + employeeId + " not found");
        }

        // Verify models exist
        var segModelResponse = modelManagementClient.getSegmentationModelById(segmentationModelId);
        if (segModelResponse.getStatusCode().is4xxClientError()) {
            throw new Exception("Segmentation model with ID " + segmentationModelId + " not found");
        }

        var recModelResponse = modelManagementClient.getRecognitionModelById(recognitionModelId);
        if (recModelResponse.getStatusCode().is4xxClientError()) {
            throw new Exception("Recognition model with ID " + recognitionModelId + " not found");
        }

        // Extract model paths from response
        JsonNode segModelNode = new ObjectMapper().readTree(segModelResponse.getBody().toString());
        JsonNode recModelNode = new ObjectMapper().readTree(recModelResponse.getBody().toString());

        String segmentationModelPath = segModelNode.get("pathName").asText();
        String recognitionModelPath = recModelNode.get("pathName").asText();

        String employeeDir = DATASET_BASE_PATH + "/" + employeeId;
        Path employeePath = Paths.get(employeeDir);

        try {
            Files.createDirectories(employeePath);
        } catch (IOException e) {
            throw new Exception("Failed to create directory for employee " + employeeId, e);
        }

        List<FingerprintSample> samples = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String position = positions.get(i);

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".bmp";

            String filename = employeeId + "_" + position + extension;
            Path filePath = Paths.get(employeeDir, filename);

            try {
                byte[] fileBytes = file.getBytes();
                Files.write(filePath, fileBytes);

                FingerprintSample sample = FingerprintSample.builder()
                        .employeeId(employeeId)
                        .image(filename)
                        .imageData(fileBytes)
                        .position(position)
                        .capturedAt(LocalDateTime.now())
                        .quality(1.0)
                        .build();

                samples.add(fingerprintSampleRepository.save(sample));

            } catch (IOException e) {
                throw new Exception("Failed to save fingerprint image: " + e.getMessage(), e);
            }
        }

        updateFingerprintModel(segmentationModelPath, recognitionModelPath);

        return samples;
    }

    public RecognitionResult recognizeFingerprint(
            MultipartFile fingerprintImage,
            String segmentationModelId,
            String recognitionModelId) throws Exception {

        // Verify models exist
        var segModelResponse = modelManagementClient.getSegmentationModelById(segmentationModelId);
        if (segModelResponse.getStatusCode().is4xxClientError()) {
            throw new Exception("Segmentation model with ID " + segmentationModelId + " not found");
        }

        var recModelResponse = modelManagementClient.getRecognitionModelById(recognitionModelId);
        if (recModelResponse.getStatusCode().is4xxClientError()) {
            throw new Exception("Recognition model with ID " + recognitionModelId + " not found");
        }

        // Extract model paths from response
        JsonNode segModelNode = new ObjectMapper().readTree(segModelResponse.getBody().toString());
        JsonNode recModelNode = new ObjectMapper().readTree(recModelResponse.getBody().toString());

        String segmentationModelPath = segModelNode.get("pathName").asText();
        String recognitionModelPath = recModelNode.get("pathName").asText();

        String tempFilePath = "temp_" + UUID.randomUUID().toString() + ".bmp";
        Path tempPath = Paths.get(tempFilePath);

        try {
            Files.write(tempPath, fingerprintImage.getBytes());
            System.out.println("Fingerprint saved temporarily to: " + tempPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save temporary fingerprint image: " + e.getMessage());
            throw new Exception("Failed to save temporary fingerprint image", e);
        }

        try {
            System.out.println("Calling recognition script for file: " + tempFilePath);
            executeRecognitionScript(tempFilePath, segmentationModelPath, recognitionModelPath);

            return readRecognitionResultFromFile();

        } catch (Exception e) {
            System.err.println("Error in fingerprint recognition process: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Failed to recognize fingerprint: " + e.getMessage(), e);
        } finally {
            try {
                boolean deleted = Files.deleteIfExists(tempPath);
                System.out.println("Temporary file deleted: " + deleted);
            } catch (IOException e) {
                System.err.println("Failed to delete temporary file: " + e.getMessage());
            }
        }
    }

    @Transactional
    public Object createAccessLog(
            String employeeId,
            String areaId,
            String accessType,
            boolean isMatched,
            double confidence,
            String segmentationModelId,
            String recognitionModelId) throws Exception {

        LocalDateTime now = LocalDateTime.now();

        // Create AccessLog via client
        Map<String, Object> accessLogData = new HashMap<>();
        accessLogData.put("areaId", areaId);
        accessLogData.put("employeeId", employeeId);
        accessLogData.put("timestamp", now);
        accessLogData.put("accessType", accessType);
        accessLogData.put("authorized", isMatched);

        var accessLogResponse = accessControlClient.createAccessLog(accessLogData);

        if (accessLogResponse.getStatusCode().is4xxClientError()) {
            throw new Exception("Failed to create access log");
        }

        // Get access log ID from response
        JsonNode accessLogNode = new ObjectMapper().readTree(accessLogResponse.getBody().toString());
        String accessLogId = accessLogNode.get("id").asText();

        // Create recognition record
        Recognition recognition = Recognition.builder()
                .employeeId(employeeId)
                .accessLogId(accessLogId)
                .fingerprintSegmentationModelId(segmentationModelId)
                .fingerprintRecognitionModelId(recognitionModelId)
                .timestamp(now)
                .confidence((float) confidence)
                .build();

        recognitionRepository.save(recognition);

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
