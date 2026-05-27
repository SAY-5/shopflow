package com.shopflow.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import com.shopflow.catalog.web.ProductView;
import com.shopflow.catalog.web.ReservationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class CatalogApiIT extends PostgresIntegrationTest {

  @Autowired private TestRestTemplate rest;

  @Test
  void listsSeededProducts() {
    ProductView[] products = rest.getForObject("/products", ProductView[].class);
    assertThat(products).isNotEmpty();
    assertThat(products[0].name()).isNotBlank();
  }

  @Test
  void reservingReducesAvailableUnits() {
    ProductView before = rest.getForObject("/products/5", ProductView.class);
    rest.postForEntity("/products/reservations", new ReservationRequest(5L, 4), Void.class);
    ProductView after = rest.getForObject("/products/5", ProductView.class);
    assertThat(after.availableUnits()).isEqualTo(before.availableUnits() - 4);
  }

  @Test
  void overReservingReturnsConflict() {
    ResponseEntity<String> response =
        rest.postForEntity(
            "/products/reservations", new ReservationRequest(4L, 100000), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void unknownProductReturnsNotFound() {
    ResponseEntity<String> response = rest.getForEntity("/products/9999", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
