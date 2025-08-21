package com.example.notification_service;

import com.example.notification_service.service.TemplateService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class NotificationServiceApplication implements CommandLineRunner {

	private final TemplateService templateService;

	public NotificationServiceApplication(TemplateService templateService) {
		this.templateService = templateService;
	}

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Initialize default notification templates on startup
		templateService.initializeDefaultTemplates();
	}

}
