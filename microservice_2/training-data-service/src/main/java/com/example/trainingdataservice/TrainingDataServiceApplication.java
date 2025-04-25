package com.example.trainingdataservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.trainingdataservice.client")
public class TrainingDataServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainingDataServiceApplication.class, args);
    }

}
