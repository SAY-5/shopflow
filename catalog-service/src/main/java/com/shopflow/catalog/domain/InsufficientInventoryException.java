package com.shopflow.catalog.domain;

public class InsufficientInventoryException extends RuntimeException {

  public InsufficientInventoryException(Long productId, int requested, int available) {
    super(
        "insufficient inventory for product "
            + productId
            + ": requested "
            + requested
            + ", available "
            + available);
  }
}
