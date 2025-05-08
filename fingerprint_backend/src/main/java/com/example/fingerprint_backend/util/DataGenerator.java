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
import com.example.fingerprint_backend.repository.biometrics.fingerprint.FingerprintSegmentationModelRepository;
import com.example.fingerprint_backend.repository.biometrics.recognition.RecognitionRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataGenerator {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final AdminRepository adminRepository;
    private final AreaRepository areaRepository;
    private final AccessLogRepository accessLogRepository;
    private final RecognitionRepository recognitionRepository;
    private final FingerprintRecognitionModelRepository fingerprintRecognitionModelRepository;
    private final FingerprintSegmentationModelRepository fingerprintSegmentationModelRepository;

    private final Faker faker = new Faker();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${fingerprint.api.url}")
    private String fingerprintApiUrl;

    // @PostConstruct
    @Transactional
    public void initializeData() throws IOException {
        clearExistingData();

        List<Area> areas = createAreas(10);
        createAdmins(3);
        List<Employee> employees = createFakeEmployees(20);

        loadModelDataFromAPI();

        System.out.println("Initialized data with " + employees.size() + " employees");

        List<Employee> employeeList = employeeRepository.findAll();
        createIdFolders(employeeList);
    }

    @Transactional
    public void clearExistingData() {
        accessLogRepository.deleteAll();
        recognitionRepository.deleteAll();
        employeeRepository.deleteAll();
        adminRepository.deleteAll();
        userRepository.deleteAll();
        areaRepository.deleteAll();
        fingerprintRecognitionModelRepository.deleteAll();
        fingerprintSegmentationModelRepository.deleteAll();
    }

    private void createIdFolders(List<Employee> employees) {
        try {
            if (employees == null || employees.isEmpty()) {
                System.out.println("No employees to create folders for");
                return;
            }

            List<String> employeeIds = employees.stream()
                    .map(Employee::getId)
                    .collect(Collectors.toList());

            System.out.println("Creating folders for " + employeeIds.size() + " employee IDs");
            System.out.println("Using API URL: " + fingerprintApiUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ids", employeeIds);
            requestBody.put("reset", true);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            String baseUrl = fingerprintApiUrl;
            if (!baseUrl.endsWith("/")) {
                baseUrl += "/";
            }

            String fullUrl = baseUrl + "api/create-id-folders/";
            System.out.println("Making request to: " + fullUrl);

            Map<String, Object> response = restTemplate.postForObject(fullUrl, request, Map.class);

            System.out.println("API Response: " + response);
        } catch (Exception e) {
            System.err.println("Error calling create-id-folders API: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadModelDataFromAPI() {
        try {
            String baseUrl = fingerprintApiUrl;
            if (!baseUrl.endsWith("/")) {
                baseUrl += "/";
            }

            String modelApiUrl = baseUrl + "api/models";
            System.out.println("Fetching models from: " + modelApiUrl);

            ResponseEntity<Map> response = restTemplate.getForEntity(modelApiUrl, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                System.err.println("Error fetching model data from API: " + response.getStatusCode());
                return;
            }

            Map<String, Object> modelData = response.getBody();

            List<Map<String, Object>> segmentationModels = (List<Map<String, Object>>) modelData.get("segmentation_models");
            if (segmentationModels != null) {
                List<FingerprintSegmentationModel> models = segmentationModels.stream()
                        .map(this::convertToSegmentationModel)
                        .collect(Collectors.toList());
                fingerprintSegmentationModelRepository.saveAll(models);
                System.out.println("Loaded " + models.size() + " segmentation models from API");
            }

            List<Map<String, Object>> recognitionModels = (List<Map<String, Object>>) modelData.get("recognition_models");
            if (recognitionModels != null) {
                List<FingerprintRecognitionModel> models = recognitionModels.stream()
                        .map(this::convertToRecognitionModel)
                        .collect(Collectors.toList());
                fingerprintRecognitionModelRepository.saveAll(models);
                System.out.println("Loaded " + models.size() + " recognition models from API");
            }

        } catch (Exception e) {
            System.err.println("Error loading model data from API: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private FingerprintSegmentationModel convertToSegmentationModel(Map<String, Object> data) {
        return FingerprintSegmentationModel.builder()
                .name((String) data.get("name"))
                .pathName((String) data.get("path_name"))
                .accuracy(((Number) data.get("accuracy")).floatValue())
                .valAccuracy(((Number) data.get("valAccuracy")).floatValue())
                .version((String) data.get("version"))
                .createdAt(LocalDateTime.parse((String) data.get("createdAt"),
                        DateTimeFormatter.ISO_DATE_TIME))
                .updatedAt(LocalDateTime.parse((String) data.get("updatedAt"),
                        DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

    private FingerprintRecognitionModel convertToRecognitionModel(Map<String, Object> data) {
        return FingerprintRecognitionModel.builder()
                .name((String) data.get("name"))
                .pathName((String) data.get("path_name"))
                .accuracy(((Number) data.get("accuracy")).floatValue())
                .valAccuracy(((Number) data.get("valAccuracy")).floatValue())
                .version((String) data.get("version"))
                .createdAt(LocalDateTime.parse((String) data.get("createdAt"),
                        DateTimeFormatter.ISO_DATE_TIME))
                .updatedAt(LocalDateTime.parse((String) data.get("updatedAt"),
                        DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

    private List<Area> createAreas(int count) {
        List<Area> areas = new ArrayList<>();

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
                    .address(faker.address().fullAddress())
                    .username(faker.name().username())
                    .email(faker.internet().emailAddress())
                    .password(faker.internet().password())
                    .build();
            admins.add(adminRepository.save(admin));
        }
        return admins;
    }

    @Transactional
    public List<Employee> createFakeEmployees(int count) {
        List<Employee> employees = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Employee employee = Employee.builder()
                    .id(UUID.randomUUID().toString())
                    .fullName(faker.name().fullName())
                    .phoneNumber(faker.phoneNumber().cellPhone())
                    .address(faker.address().fullAddress())
                    .maxNumberSamples(5)
                    .build();

            employee.setAccessLogs(new ArrayList<>());
            employee.setRecognitions(new ArrayList<>());

            employee = employeeRepository.save(employee);
            employees.add(employee);
        }

        return employees;
    }
}