package com.shopflow.orders.domain;

public class OrderPlacementException extends RuntimeException {

  public OrderPlacementException(String message, Throwable cause) {
    super(message, cause);
  }
}
