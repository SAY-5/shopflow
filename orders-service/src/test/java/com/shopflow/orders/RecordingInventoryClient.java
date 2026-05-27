package com.shopflow.orders;

import com.shopflow.orders.client.InventoryClient;
import com.shopflow.orders.client.InventoryException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory inventory client used by integration tests. Tracks reserved units per product so a test
 * can assert that compensation released everything, and can be told to fail on a chosen product to
 * inject a mid-saga failure.
 */
public class RecordingInventoryClient implements InventoryClient {

  private final Map<Long, Integer> reserved = new ConcurrentHashMap<>();
  private final Set<Long> failOnReserve = ConcurrentHashMap.newKeySet();

  @Override
  public void reserve(Long productId, int units) {
    if (failOnReserve.contains(productId)) {
      throw new InventoryException("injected reservation failure for product " + productId);
    }
    reserved.merge(productId, units, Integer::sum);
  }

  @Override
  public void release(Long productId, int units) {
    reserved.merge(productId, -units, Integer::sum);
  }

  public void failReservationsFor(Long productId) {
    failOnReserve.add(productId);
  }

  public int reservedUnits(Long productId) {
    return reserved.getOrDefault(productId, 0);
  }

  public Map<Long, Integer> snapshot() {
    return new HashMap<>(reserved);
  }

  public void reset() {
    reserved.clear();
    failOnReserve.clear();
  }
}
