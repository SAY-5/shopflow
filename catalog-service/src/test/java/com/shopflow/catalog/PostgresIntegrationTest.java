package com.shopflow.catalog;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for integration tests. A single Postgres container is started once for the whole JVM
 * and reused across every test class, which keeps the Spring context cache stable (a per-class
 * container would change the JDBC port between contexts and force a fresh context each time).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class PostgresIntegrationTest {

  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("catalog")
          .withUsername("catalog")
          .withPassword("catalog");

  static {
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }
}
