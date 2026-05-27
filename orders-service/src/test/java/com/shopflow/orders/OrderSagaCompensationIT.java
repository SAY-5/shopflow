package com.shopflow.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shopflow.orders.domain.OrderPlacementException;
import com.shopflow.orders.domain.OrderRepository;
import com.shopflow.orders.domain.OrderService;
import com.shopflow.orders.domain.PlaceOrderCommand;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Proves the compensation half of the order placement saga. The first line reserves successfully,
 * then the second line's reservation is made to fail mid saga. The saga must release the
 * reservation it already made and must not leave a partial order in the database.
 */
class OrderSagaCompensationIT extends PostgresIntegrationTest {

  @Autowired private OrderService orders;
  @Autowired private RecordingInventoryClient inventory;
  @Autowired private OrderRepository orderRepository;

  @BeforeEach
  void resetInventory() {
    inventory.reset();
  }

  private PlaceOrderCommand twoLineOrder() {
    return new PlaceOrderCommand(
        "erin",
        List.of(
            new PlaceOrderCommand.Line(10L, "Reserves fine", new BigDecimal("5.00"), 2),
            new PlaceOrderCommand.Line(11L, "Fails to reserve", new BigDecimal("7.00"), 1)));
  }

  @Test
  void midSagaFailureReleasesReservationsAndPersistsNoOrder() {
    inventory.failReservationsFor(11L);
    long ordersBefore = orderRepository.count();

    assertThatThrownBy(() -> orders.placeOrder(twoLineOrder()))
        .isInstanceOf(OrderPlacementException.class);

    assertThat(inventory.reservedUnits(10L))
        .as("the first reservation must be compensated back to zero")
        .isEqualTo(0);
    assertThat(inventory.snapshot().values())
        .as("no product may be left with units reserved")
        .allMatch(units -> units == 0);
    assertThat(orderRepository.count())
        .as("no partial order may be persisted")
        .isEqualTo(ordersBefore);
  }

  @Test
  void successfulSagaKeepsReservationsAndPersistsOrder() {
    long ordersBefore = orderRepository.count();
    orders.placeOrder(
        new PlaceOrderCommand(
            "erin",
            List.of(new PlaceOrderCommand.Line(10L, "Reserves fine", new BigDecimal("5.00"), 3))));

    assertThat(inventory.reservedUnits(10L)).isEqualTo(3);
    assertThat(orderRepository.count()).isEqualTo(ordersBefore + 1);
  }
}
