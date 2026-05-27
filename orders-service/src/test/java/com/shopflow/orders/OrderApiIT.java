package com.shopflow.orders;

import static org.assertj.core.api.Assertions.assertThat;

import com.shopflow.orders.web.OrderView;
import com.shopflow.orders.web.PlaceOrderRequest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class OrderApiIT extends PostgresIntegrationTest {

  @Autowired private TestRestTemplate rest;
  @Autowired private RecordingInventoryClient inventory;

  @BeforeEach
  void resetInventory() {
    inventory.reset();
  }

  private PlaceOrderRequest sampleOrder(String customer) {
    return new PlaceOrderRequest(
        customer,
        List.of(
            new PlaceOrderRequest.Line(1L, "Mug", new BigDecimal("18.00"), 2),
            new PlaceOrderRequest.Line(2L, "Tote", new BigDecimal("24.00"), 1)));
  }

  @Test
  void placingOrderReservesInventoryAndComputesTotal() {
    ResponseEntity<OrderView> response =
        rest.postForEntity("/orders", sampleOrder("alice"), OrderView.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    OrderView order = response.getBody();
    assertThat(order.status()).isEqualTo("PENDING");
    assertThat(order.total()).isEqualByComparingTo("60.00");
    assertThat(inventory.reservedUnits(1L)).isEqualTo(2);
  }

  @Test
  void orderHistoryReturnsCustomerOrders() {
    rest.postForEntity("/orders", sampleOrder("bob"), OrderView.class);
    OrderView[] history = rest.getForObject("/orders?customerRef=bob", OrderView[].class);
    assertThat(history).hasSizeGreaterThanOrEqualTo(1);
  }

  @Test
  void orderCanBeConfirmedThenShipped() {
    OrderView placed = rest.postForObject("/orders", sampleOrder("carol"), OrderView.class);
    rest.postForObject(
        "/orders/" + placed.id() + "/transitions?to=CONFIRMED", null, OrderView.class);
    OrderView shipped =
        rest.postForObject(
            "/orders/" + placed.id() + "/transitions?to=SHIPPED", null, OrderView.class);
    assertThat(shipped.status()).isEqualTo("SHIPPED");
  }
}
