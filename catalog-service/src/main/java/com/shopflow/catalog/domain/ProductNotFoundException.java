package com.shopflow.catalog.domain;

public class ProductNotFoundException extends RuntimeException {

  public ProductNotFoundException(Long productId) {
    super("product not found: " + productId);
  }
}
