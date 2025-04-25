package com.example.modelmanagementservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "biometrics-service")
public interface BiometricsClient {

    @GetMapping("/api/recognitions/model/{modelId}")
    List<Map<String, Object>> getRecognitionsByModelId(@PathVariable String modelId);
}
