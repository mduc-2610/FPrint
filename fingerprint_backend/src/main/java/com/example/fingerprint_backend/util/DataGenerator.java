package com.example.fingerprint_backend.util;

import com.example.fingerprint_backend.model.access.Area;
import com.example.fingerprint_backend.model.auth.Admin;
import com.example.fingerprint_backend.model.auth.Employee;
import com.example.fingerprint_backend.model.biometrics.fingerprint.*;
import com.example.fingerprint_backend.repository.access.AccessLogRepository;
import com.example.fingerprint_backend.repository.access.AreaRepository;
import com.example.fingerprint_backend.repository.auth.AdminRepository;
import com.example.fingerprint_backend.repository.auth.EmployeeRepository;
import com.example.fingerprint_backend.repository.base.UserRepository;
import com.example.fingerprint_backend.repository.biometrics.fingerprint.FingerprintRecognitionModelRepository;
import com.example.fingerprint_backend.repository.biometrics.fingerprint.FingerprintSampleRepository;
import com.example.fingerprint_backend.repository.biometrics.fingerprint.FingerprintSegmentationModelRepository;
import com.example.fingerprint_backend.repository.biometrics.recognition.RecognitionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DataGenerator {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final AdminRepository adminRepository;
    private final AreaRepository areaRepository;
    private final AccessLogRepository accessLogRepository;
    private final RecognitionRepository recognitionRepository;
    private final FingerprintSampleRepository fingerprintSampleRepository;
    private final FingerprintRecognitionModelRepository fingerprintRecognitionModelRepository;
    private final FingerprintSegmentationModelRepository fingerprintSegmentationModelRepository;

    private final Faker faker = new Faker();
    private final Random random = new Random();
    private final ObjectMapper objectMapper;

    private static final String reportPath = "reports/";

    // @PostConstruct
    public void initializeData() throws IOException {

        accessLogRepository.deleteAll();
        fingerprintSampleRepository.deleteAll();
        employeeRepository.deleteAll();
        adminRepository.deleteAll();
        userRepository.deleteAll();
        areaRepository.deleteAll();
        fingerprintRecognitionModelRepository.deleteAll();
        fingerprintSegmentationModelRepository.deleteAll();
        recognitionRepository.deleteAll();
        if (employeeRepository.count() > 0) {
            return;
        }

        List<Area> areas = createAreas(10);


        List<Admin> admins = createAdmins(3);

        List<Employee> employees = createFakeEmployees(20);

        List<FingerprintRecognitionModel> recognitionModel = loadRecognitionModel(
                "fingerprint_recognition_models.json");
        List<FingerprintSegmentationModel> segmentationModel = loadSegmentationModel(
                "fingerprint_segmentation_models.json");


        // createFSegmentationTData(segmentationModel, trainingData);
        // createFRecognitionTData(recognitionModel, trainingData);

        // List<CameraImage> cameraImages = createCameraImages(cameras);

        // List<AccessLog> accessLogs = createAccessLogs(employees, areas);

        System.out.println("Initialized data with " + employees.size() + " employees");
    }

    private String getRandomFingerPosition() {
        String[] positions = {
                "right_thumb", "right_index", "right_middle", "right_ring", "right_little",
                "left_thumb", "left_index", "left_middle", "left_ring", "left_little"
        };
        return positions[random.nextInt(positions.length)];
    }

    private List<Area> createAreas(int count) {
        List<Area> areas = new ArrayList<>();

        // Define real area data
        List<String[]> realAreas = new ArrayList<>();
        realAreas.add(new String[] { "Research and Development Lab",
                "High-security laboratory for product research and development", "5" });
        realAreas.add(
                new String[] { "Executive Office Suite", "Executive management offices and conference rooms", "5" });
        realAreas.add(new String[] { "Server Room", "Primary data center and server infrastructure", "5" });
        realAreas.add(new String[] { "Finance Department", "Financial records and accounting offices", "4" });
        realAreas.add(new String[] { "Human Resources", "Personnel files and HR management offices", "4" });
        realAreas.add(new String[] { "Manufacturing Floor", "Main production and assembly area", "3" });
        realAreas.add(new String[] { "Quality Control Lab", "Testing and quality assurance facilities", "3" });
        realAreas.add(new String[] { "Warehouse", "Inventory storage and shipping facilities", "2" });
        realAreas.add(new String[] { "Main Lobby", "Reception area and visitor check-in", "1" });
        realAreas.add(new String[] { "Employee Cafeteria", "Staff dining and break areas", "1" });

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


    private List<Admin> createAdmins(int count) {
        List<Admin> admins = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Admin admin = Admin.builder()
                    .id(UUID.randomUUID().toString())
                    .fullName(faker.name().fullName())
                    .phoneNumber(faker.phoneNumber().cellPhone())
                    .photo("admin_photo_" + i + ".jpg")
                    .address(faker.address().fullAddress())
                    .username(faker.name().username())
                    .email(faker.internet().emailAddress())
                    .password(faker.internet().password())
                    .build();
            admins.add(adminRepository.save(admin));
        }
        return admins;
    }

    private List<Employee> createFakeEmployees(int count) {
        List<Employee> employees = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Employee employee = Employee.builder()
                    .id(UUID.randomUUID().toString())
                    .fullName(faker.name().fullName())
                    .phoneNumber(faker.phoneNumber().cellPhone())
                    .photo("employee_photo_" + i + ".jpg")
                    .address(faker.address().fullAddress())
                    .maxNumberSamples(5)
                    .build();
            employees.add(employeeRepository.save(employee));
        }

        return employees;
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
                    .createdAt(LocalDateTime.parse(jsonObject.getString("createdAt"),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .updatedAt(LocalDateTime.parse(jsonObject.getString("updatedAt"),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME))
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
                    .createdAt(LocalDateTime.parse(jsonObject.getString("createdAt"),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .updatedAt(LocalDateTime.parse(jsonObject.getString("updatedAt"),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
            models.add(model);
        }
        fingerprintSegmentationModelRepository.saveAll(models);

        return models;
    }


}