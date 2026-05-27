package com.shopflow.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * A failing downstream should trip the catalog circuit breaker so the gateway answers from its
 * fallback rather than passing the failure (or a hang) through to the caller.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CircuitBreakerTest {

  static MockWebServer catalog;
  static MockWebServer cart;
  static MockWebServer orders;

  @Autowired private WebTestClient client;

  @BeforeAll
  static void startServers() throws IOException {
    catalog = new MockWebServer();
    cart = new MockWebServer();
    orders = new MockWebServer();
    catalog.start();
    cart.start();
    orders.start();
    // The catalog downstream is down: every call errors.
    catalog.setDispatcher(
        new okhttp3.mockwebserver.Dispatcher() {
          @Override
          public MockResponse dispatch(okhttp3.mockwebserver.RecordedRequest request) {
            return new MockResponse().setResponseCode(500);
          }
        });
  }

  @AfterAll
  static void stopServers() throws IOException {
    catalog.shutdown();
    cart.shutdown();
    try {
      orders.shutdown();
    } catch (IllegalStateException alreadyStopped) {
      // a test may have shut this one down to simulate an outage
    }
  }

  @DynamicPropertySource
  static void routes(DynamicPropertyRegistry registry) {
    registry.add("CATALOG_URI", () -> "http://localhost:" + catalog.getPort());
    registry.add("CART_URI", () -> "http://localhost:" + cart.getPort());
    registry.add("ORDERS_URI", () -> "http://localhost:" + orders.getPort());
  }

  @Test
  void failingCatalogTripsTheBreakerAndGatewayReturnsFallback() {
    // Drive enough calls through to open the breaker, then confirm the gateway
    // degrades to its fallback response.
    HttpStatus last = null;
    for (int i = 0; i < 12; i++) {
      last =
          HttpStatus.valueOf(
              client
                  .get()
                  .uri("/api/catalog/products")
                  .exchange()
                  .returnResult(String.class)
                  .getStatus()
                  .value());
    }
    assertThat(last).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);

    int requestsBeforeOpen = catalog.getRequestCount();
    // Once the breaker is open it short circuits, so a further call should not reach the
    // downstream.
    client
        .get()
        .uri("/api/catalog/products")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    assertThat(catalog.getRequestCount())
        .as("an open breaker should stop forwarding to the failing downstream")
        .isEqualTo(requestsBeforeOpen);
  }

  @Test
  void healthyServiceIsNotAffectedByAnotherServiceBreaker() {
    cart.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
    client.get().uri("/api/cart/carts/abc").exchange().expectStatus().isOk();
  }

  @Test
  void totalOutageOfADownstreamReturnsFallbackQuicklyInsteadOfHanging() throws IOException {
    // The orders downstream is entirely gone (connection refused), the worst case for a hang.
    orders.shutdown();

    long start = System.nanoTime();
    client
        .get()
        .uri("/api/orders/orders?customerRef=alice")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

    assertThat(elapsedMillis)
        .as("the gateway should fall back well within the configured timeout")
        .isLessThan(3_000);
  }
}
