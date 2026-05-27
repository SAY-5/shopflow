package com.shopflow.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutingTest {

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
  }

  @AfterAll
  static void stopServers() throws IOException {
    catalog.shutdown();
    cart.shutdown();
    orders.shutdown();
  }

  @DynamicPropertySource
  static void routes(DynamicPropertyRegistry registry) {
    registry.add("CATALOG_URI", () -> "http://localhost:" + catalog.getPort());
    registry.add("CART_URI", () -> "http://localhost:" + cart.getPort());
    registry.add("ORDERS_URI", () -> "http://localhost:" + orders.getPort());
  }

  @Test
  void catalogRequestIsForwardedWithPrefixStripped() throws InterruptedException {
    catalog.enqueue(new MockResponse().setResponseCode(200).setBody("[]"));
    client.get().uri("/api/catalog/products").exchange().expectStatus().isOk();
    RecordedRequest forwarded = catalog.takeRequest();
    assertThat(forwarded.getPath()).isEqualTo("/products");
  }

  @Test
  void ordersRequestIsRoutedToOrdersService() throws InterruptedException {
    orders.enqueue(new MockResponse().setResponseCode(200).setBody("[]"));
    client.get().uri("/api/orders/orders?customerRef=alice").exchange().expectStatus().isOk();
    RecordedRequest forwarded = orders.takeRequest();
    assertThat(forwarded.getPath()).isEqualTo("/orders?customerRef=alice");
  }

  @Test
  void cartPostIsForwardedWithMethodAndBodyPreserved() throws InterruptedException {
    cart.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
    client
        .post()
        .uri("/api/cart/carts/abc/items")
        .bodyValue("{\"productId\":1,\"quantity\":2}")
        .exchange()
        .expectStatus()
        .isOk();
    RecordedRequest forwarded = cart.takeRequest();
    assertThat(forwarded.getMethod()).isEqualTo("POST");
    assertThat(forwarded.getPath()).isEqualTo("/carts/abc/items");
    assertThat(forwarded.getBody().readUtf8()).contains("\"productId\":1");
  }

  @Test
  void unknownPathIsNotFound() {
    client.get().uri("/api/unknown/thing").exchange().expectStatus().isNotFound();
  }
}
