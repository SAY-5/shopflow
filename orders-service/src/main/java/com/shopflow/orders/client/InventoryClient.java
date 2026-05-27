package com.shopflow.orders.client;

/** Abstraction over the catalog service inventory operations used when placing an order. */
public interface InventoryClient {

  void reserve(Long productId, int units);

  void release(Long productId, int units);
}
