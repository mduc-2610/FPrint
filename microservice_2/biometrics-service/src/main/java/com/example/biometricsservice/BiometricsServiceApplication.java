package com.example.biometricsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.biometricsservice.client")
public class BiometricsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiometricsServiceApplication.class, args);
    }

}
