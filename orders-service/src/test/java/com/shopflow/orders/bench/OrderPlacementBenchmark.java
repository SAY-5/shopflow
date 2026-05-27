package com.shopflow.orders.bench;

import com.shopflow.orders.RecordingInventoryClient;
import com.shopflow.orders.TestInventoryConfig;
import com.shopflow.orders.domain.OrderService;
import com.shopflow.orders.domain.PlaceOrderCommand;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Measures the order placement path against a real Postgres. The recording inventory client stands
 * in for the catalog reservation calls so the number reflects the orders service work (saga
 * bookkeeping plus the database write), not network time to another service.
 *
 * <p>Tagged {@code bench} so it is skipped in the normal build and only runs from the benchmark
 * script. It writes the measured latencies to a JSON file for the regression gate to compare.
 */
@SpringBootTest
@Import(TestInventoryConfig.class)
@Tag("bench")
class OrderPlacementBenchmark {

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

  private static final int WARMUP = 200;
  private static final int MEASURED = 2000;

  @Autowired private OrderService orders;
  @Autowired private RecordingInventoryClient inventory;

  private PlaceOrderCommand command() {
    return new PlaceOrderCommand(
        "bench",
        List.of(
            new PlaceOrderCommand.Line(1L, "Field Notebook", new BigDecimal("12.50"), 1),
            new PlaceOrderCommand.Line(2L, "Ceramic Mug", new BigDecimal("18.00"), 2)));
  }

  @Test
  void measurePlacementLatency() throws IOException {
    inventory.reset();
    for (int i = 0; i < WARMUP; i++) {
      orders.placeOrder(command());
    }

    long[] samplesNanos = new long[MEASURED];
    long start = System.nanoTime();
    for (int i = 0; i < MEASURED; i++) {
      long t0 = System.nanoTime();
      orders.placeOrder(command());
      samplesNanos[i] = System.nanoTime() - t0;
    }
    long wallNanos = System.nanoTime() - start;

    Arrays.sort(samplesNanos);
    double p50 = samplesNanos[(int) (MEASURED * 0.50)] / 1_000_000.0;
    double p95 = samplesNanos[(int) (MEASURED * 0.95)] / 1_000_000.0;
    double p99 = samplesNanos[(int) (MEASURED * 0.99)] / 1_000_000.0;
    double throughput = MEASURED / (wallNanos / 1_000_000_000.0);

    String json =
        String.format(
            Locale.ROOT,
            "{%n"
                + "  \"benchmark\": \"order-placement\",%n"
                + "  \"samples\": %d,%n"
                + "  \"p50_ms\": %.3f,%n"
                + "  \"p95_ms\": %.3f,%n"
                + "  \"p99_ms\": %.3f,%n"
                + "  \"throughput_ops_per_sec\": %.1f%n"
                + "}%n",
            MEASURED,
            p50,
            p95,
            p99,
            throughput);

    Path out = Path.of(System.getProperty("bench.out", "target/bench-result.json"));
    Files.createDirectories(out.toAbsolutePath().getParent());
    Files.writeString(out, json);
    System.out.print(json);
  }
}
