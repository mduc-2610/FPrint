package com.example.modelmanagementservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(
    name = "biometrics-service",
    url = "${biometrics-service.url}"
)
public interface BiometricsClient {

    @GetMapping("/recognition/by-recognition-model/{modelId}")
    List<Map<String, Object>> getRecognitionsByRecognitionModelId(@PathVariable String modelId);
    
    @GetMapping("/recognition/by-segmentation-model/{modelId}")
    List<Map<String, Object>> getRecognitionsBySegmentationModelId(@PathVariable String modelId);
    
    @GetMapping("/fingerprint/by-recognition-model/{modelId}")
    List<Map<String, Object>> getFingerprintSamplesByRecognitionModelId(@PathVariable String modelId);
    
    @GetMapping("/fingerprint/by-segmentation-model/{modelId}")
    List<Map<String, Object>> getFingerprintSamplesBySegmentationModelId(@PathVariable String modelId);
}