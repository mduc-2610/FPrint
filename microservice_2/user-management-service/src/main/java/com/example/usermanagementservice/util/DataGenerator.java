package com.example.usermanagementservice.util;

import com.example.usermanagementservice.model.Admin;
import com.example.usermanagementservice.model.Employee;
import com.example.usermanagementservice.repository.AdminRepository;
import com.example.usermanagementservice.repository.EmployeeRepository;
import com.example.usermanagementservice.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final AdminRepository adminRepository;

    private final Faker faker = new Faker();
    private final Random random = new Random();
    private final ObjectMapper objectMapper;

    private static final String reportPath = "../fingerprint_training/reports/";

    @Value("${data.generator.enabled}")
    private boolean dataGeneratorEnabled;

    @PostConstruct
    public void initializeData() throws IOException {
        if (!dataGeneratorEnabled) {
            System.out.println("Data generator is disabled. Skipping data initialization.");
            return;
        }


        employeeRepository.deleteAll();
        adminRepository.deleteAll();
        userRepository.deleteAll();

        List<Admin> admins = createAdmins(3);

        // List<Employee> employees = createEmployeesFromDataset();
        List<Employee> employees = createEmployees(20);


    }

    private List<Employee> createEmployeesFromDataset() {
        List<Employee> employees = new ArrayList<>();

        File datasetDir = new File("fingerprint_adapting_test_dataset");
        File[] datasetFolders = datasetDir.listFiles(File::isDirectory);

        if (datasetFolders == null || datasetFolders.length == 0) {
            System.out.println("No dataset folders found in fingerprint_adapting_test_dataset");
            return employees;
        }

        for (File folder : datasetFolders) {
            String datasetId = folder.getName();

            Employee employee = Employee.builder()
                    .id(UUID.randomUUID().toString())
                    .fullName(faker.name().fullName())
                    .phoneNumber(faker.phoneNumber().cellPhone())
                    .username(faker.name().username())
                    .email(faker.internet().emailAddress())
                    .password(faker.internet().password())
                    .build();

            employees.add(employeeRepository.save(employee));
        }

        System.out.println("Loaded " + employees.size() + " employees with fingerprint samples from dataset");
        return employees;
    }
    
    private List<Employee> createEmployees(int count) {
        List<Employee> employees = new ArrayList<>();

        for (int i = 0; i < count; i++) {

            Employee employee = Employee.builder()
                    .id(UUID.randomUUID().toString())
                    .fullName(faker.name().fullName())
                    .phoneNumber(faker.phoneNumber().cellPhone())
                    .username(faker.name().username())
                    .email(faker.internet().emailAddress())
                    .password(faker.internet().password())
                    .build();

            employees.add(employeeRepository.save(employee));
        }

        System.out.println("Loaded " + employees.size() + " employees with fingerprint samples from dataset");
        return employees;
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

}