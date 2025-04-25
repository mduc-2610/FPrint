package com.example.biometricsservice.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "access-control-service",
    url = "${access-control-service.url}"    
)
public interface AccessControlClient {

    @GetMapping("/area/{id}")
    ResponseEntity<Object> getAreaById(@PathVariable String id);
    
    @PostMapping("/access-log")
    ResponseEntity<Map<String, Object>> createAccessLog(@RequestBody Object accessLog);

    @PutMapping("/access-log/{id}/recognition-id")
    ResponseEntity<Object> updateRecognitionId(@PathVariable String id, @RequestBody String recognitionId);
}
