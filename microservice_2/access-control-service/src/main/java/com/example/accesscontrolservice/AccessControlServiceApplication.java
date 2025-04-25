package com.example.accesscontrolservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.accesscontrolservice.client")
public class AccessControlServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccessControlServiceApplication.class, args);
    }

}
