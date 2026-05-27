package com.shopflow.orders.domain;

public class OrderNotFoundException extends RuntimeException {

  public OrderNotFoundException(Long orderId) {
    super("order not found: " + orderId);
  }
}
