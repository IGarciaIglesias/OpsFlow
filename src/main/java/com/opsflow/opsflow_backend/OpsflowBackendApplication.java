package com.opsflow.opsflow_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@CrossOrigin(origins = "http://localhost:4200")
public class OpsflowBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpsflowBackendApplication.class, args);
	}

}
