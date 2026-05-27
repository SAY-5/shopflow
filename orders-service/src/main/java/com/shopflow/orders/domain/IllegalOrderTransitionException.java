package com.shopflow.orders.domain;

public class IllegalOrderTransitionException extends RuntimeException {

  public IllegalOrderTransitionException(OrderStatus from, OrderStatus to) {
    super("cannot transition order from " + from + " to " + to);
  }
}
