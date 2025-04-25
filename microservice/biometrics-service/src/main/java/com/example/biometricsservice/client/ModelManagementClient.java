package com.example.biometricsservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "model-management-service",
    url = "${model-management-service.url:http://localhost:8084}"
)
public interface ModelManagementClient {

    @GetMapping("/api/fingerprint-segmentation-models/{id}")
    ResponseEntity<Object> getSegmentationModelById(@PathVariable String id);

    @GetMapping("/api/fingerprint-recognition-models/{id}")
    ResponseEntity<Object> getRecognitionModelById(@PathVariable String id);
}
