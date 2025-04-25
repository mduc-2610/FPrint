package com.example.fingerprint_backend.service;

import com.example.fingerprint_backend.model.biometrics.fingerprint.FingerprintSample;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class FingerprintManagementService {

    private static final String ADAPTING_DATASET_BASE_PATH = "../../../../../../../../AI-modal-service/fingerprint_adapting_dataset";
    private static final String DISABLED_BASE_PATH = "./../../../../../../../AI-modal-service/fingerprint_disabled";

    public void moveFileToDisabledDirectory(String image, String employeeId) throws IOException {
        Path disabledDir = Paths.get(DISABLED_BASE_PATH + employeeId);
        Files.createDirectories(disabledDir);

        Path sourcePath = Paths.get(ADAPTING_DATASET_BASE_PATH + employeeId + "/" + image);
        Path destPath = disabledDir.resolve(image);

        Files.move(sourcePath, destPath);
    }

    public void moveFileToAdaptingDirectory(String image, String employeeId) throws IOException {
        Path adaptingDir = Paths.get(ADAPTING_DATASET_BASE_PATH + employeeId);
        Files.createDirectories(adaptingDir);

        Path sourcePath = Paths.get(DISABLED_BASE_PATH + employeeId + "/" + image);
        Path destPath = adaptingDir.resolve(image);

        Files.move(sourcePath, destPath);
    }

    public void moveAllActiveFilesToDisabledDirectory(List<FingerprintSample> activeSamples, String employeeId) throws IOException {
        Path disabledDir = Paths.get(DISABLED_BASE_PATH + employeeId);
        Files.createDirectories(disabledDir);

        for (FingerprintSample sample : activeSamples) {
            Path sourcePath = Paths.get(ADAPTING_DATASET_BASE_PATH + employeeId + "/" + sample.getImage());
            Path destPath = disabledDir.resolve(sample.getImage());

            if (Files.exists(sourcePath)) {
                Files.move(sourcePath, destPath);
            }
        }
    }

    public void deleteFingerprintImage(String image, String employeeId) throws IOException {
        Path adaptingPath = Paths.get(ADAPTING_DATASET_BASE_PATH + employeeId + "/" + image);
        Path disabledPath = Paths.get(DISABLED_BASE_PATH + employeeId + "/" + image);

        if (Files.exists(adaptingPath)) {
            Files.delete(adaptingPath);
        } else if (Files.exists(disabledPath)) {
            Files.delete(disabledPath);
        } else {
            throw new IOException("Image file not found in both adapting and disabled directories.");
        }
    }

}