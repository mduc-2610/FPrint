package com.example.biometricsservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "access-control-service",
    url = "${access-control-service.url:http://localhost:8081}"    
)
public interface AccessControlClient {

    @GetMapping("/api/areas/{id}")
    ResponseEntity<Object> getAreaById(@PathVariable String id);

    @PostMapping("/api/access-logs")
    ResponseEntity<Object> createAccessLog(@RequestBody Object accessLog);
}
