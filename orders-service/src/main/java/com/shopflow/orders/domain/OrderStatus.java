package com.shopflow.orders.domain;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Order lifecycle states and the transitions allowed between them. */
public enum OrderStatus {
  PENDING,
  CONFIRMED,
  SHIPPED,
  CANCELLED;

  private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED =
      Map.of(
          PENDING, EnumSet.of(CONFIRMED, CANCELLED),
          CONFIRMED, EnumSet.of(SHIPPED, CANCELLED),
          SHIPPED, EnumSet.noneOf(OrderStatus.class),
          CANCELLED, EnumSet.noneOf(OrderStatus.class));

  public boolean canTransitionTo(OrderStatus target) {
    return ALLOWED.get(this).contains(target);
  }
}
