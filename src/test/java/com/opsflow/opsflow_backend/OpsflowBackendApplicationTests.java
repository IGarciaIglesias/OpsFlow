package com.opsflow.opsflow_backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("CI: este test arranca el contexto completo y requiere PostgreSQL/Flyway.")
@SpringBootTest
class OpsflowBackendApplicationTests {

	@Test
	void contextLoads() {
	}
}