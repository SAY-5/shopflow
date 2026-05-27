package com.shopflow.cart.domain;

public class CartItemNotFoundException extends RuntimeException {

  public CartItemNotFoundException(String sessionId, Long productId) {
    super("cart item not found for session " + sessionId + " and product " + productId);
  }
}
