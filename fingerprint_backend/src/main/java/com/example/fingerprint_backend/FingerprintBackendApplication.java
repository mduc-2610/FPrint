package com.example.fingerprint_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@SpringBootApplication
public class FingerprintBackendApplication {

//	public static void main(String[] args) {
//		SpringApplication.run(FingerprintBackendApplication.class, args);
//	}


	public static void main(String[] args) {
		// Create necessary directories
		createDirectoryIfNotExists("temp_fingerprints");

		SpringApplication.run(FingerprintBackendApplication.class, args);
	}

	private static void createDirectoryIfNotExists(String directoryPath) {
		File directory = new File(directoryPath);
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins("*")
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
						.allowedHeaders("*")
						.maxAge(3600);
			}
		};
	}
}
