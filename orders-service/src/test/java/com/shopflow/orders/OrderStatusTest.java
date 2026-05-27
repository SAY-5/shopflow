package com.shopflow.orders;

import static org.assertj.core.api.Assertions.assertThat;

import com.shopflow.orders.domain.OrderStatus;
import org.junit.jupiter.api.Test;

class OrderStatusTest {

  @Test
  void pendingCanBeConfirmedOrCancelled() {
    assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.CONFIRMED)).isTrue();
    assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
  }

  @Test
  void pendingCannotShipDirectly() {
    assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.SHIPPED)).isFalse();
  }

  @Test
  void shippedIsTerminal() {
    assertThat(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
    assertThat(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.CONFIRMED)).isFalse();
  }
}
