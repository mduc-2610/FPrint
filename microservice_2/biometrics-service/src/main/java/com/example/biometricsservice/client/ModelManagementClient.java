package com.example.biometricsservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "model-management-service",
    url = "${model-management-service.url}"
)
public interface ModelManagementClient {

    @GetMapping("/fingerprint-segmentation-model/{id}")
    ResponseEntity<Object> getSegmentationModelById(@PathVariable String id);

    @GetMapping("/fingerprint-recognition-model/{id}")
    ResponseEntity<Object> getRecognitionModelById(@PathVariable String id);
}
