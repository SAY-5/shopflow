package com.shopflow.orders;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for integration tests. One Postgres container is started for the whole JVM and reused
 * by every test class so the Spring context cache stays warm. The recording inventory client stands
 * in for the catalog service so order placement can be exercised without a live downstream.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestInventoryConfig.class)
public abstract class PostgresIntegrationTest {

  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("orders")
          .withUsername("orders")
          .withPassword("orders");

  static {
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("shopflow.catalog.base-url", () -> "http://localhost:0");
  }
}
