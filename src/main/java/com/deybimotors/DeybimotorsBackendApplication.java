package com.deybimotors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DeybimotorsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeybimotorsBackendApplication.class, args);
		System.out.println("\n========================================");
		System.out.println("🚀 DebyMotors Backend Iniciado");
		System.out.println("========================================");
		System.out.println("🌐 URL: http://localhost:8080");
		System.out.println("📄 Docs API: http://localhost:8080/swagger-ui.html");
		System.out.println("========================================\n");
	}
}