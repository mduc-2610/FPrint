package com.example.fingerprint_backend.controller.util;

import com.example.fingerprint_backend.util.DataGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataGeneratorController {

    private final DataGenerator dataGenerator;

    @PostMapping("/reset")
    public ResponseEntity<String> resetData() throws IOException {
        dataGenerator.initializeData();
        return ResponseEntity.ok("Data reset successfully!");
    }
}
