package com.shopflow.orders;

import static org.assertj.core.api.Assertions.assertThat;

import com.shopflow.orders.domain.OrderRepository;
import com.shopflow.orders.web.PlaceOrderRequest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Order placement must validate inventory before an order is committed. */
class OrderPlacementInventoryIT extends PostgresIntegrationTest {

  @Autowired private TestRestTemplate rest;
  @Autowired private RecordingInventoryClient inventory;
  @Autowired private OrderRepository orders;

  @BeforeEach
  void resetInventory() {
    inventory.reset();
  }

  @Test
  void placementFailsWhenInventoryRejectsReservation() {
    inventory.failReservationsFor(7L);
    long before = orders.count();

    PlaceOrderRequest request =
        new PlaceOrderRequest(
            "dave",
            List.of(
                new PlaceOrderRequest.Line(7L, "Out of stock item", new BigDecimal("9.99"), 1)));
    ResponseEntity<String> response = rest.postForEntity("/orders", request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(orders.count()).isEqualTo(before);
  }
}
