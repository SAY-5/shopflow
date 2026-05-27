package com.shopflow.orders.domain;

import com.shopflow.orders.client.InventoryClient;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Places orders using a saga across the catalog and orders services. Inventory is reserved line by
 * line in the catalog service first; if any later step fails, the reservations made so far are
 * released by running their compensating actions in reverse order so no stock stays locked and no
 * partial order is persisted.
 */
@Service
public class OrderService {

  private static final Logger log = LoggerFactory.getLogger(OrderService.class);

  private final OrderRepository orders;
  private final OrderWriter writer;
  private final InventoryClient inventory;

  public OrderService(OrderRepository orders, OrderWriter writer, InventoryClient inventory) {
    this.orders = orders;
    this.writer = writer;
    this.inventory = inventory;
  }

  public Order placeOrder(PlaceOrderCommand command) {
    if (command.lines() == null || command.lines().isEmpty()) {
      throw new IllegalArgumentException("an order needs at least one line");
    }

    Deque<Runnable> compensations = new ArrayDeque<>();
    try {
      for (PlaceOrderCommand.Line line : command.lines()) {
        inventory.reserve(line.productId(), line.quantity());
        compensations.push(() -> inventory.release(line.productId(), line.quantity()));
      }
      Order saved = writer.persist(command);
      return get(saved.getId());
    } catch (RuntimeException failure) {
      compensate(compensations);
      throw new OrderPlacementException("order placement failed and was rolled back", failure);
    }
  }

  private void compensate(Deque<Runnable> compensations) {
    while (!compensations.isEmpty()) {
      Runnable action = compensations.pop();
      try {
        action.run();
      } catch (RuntimeException compensationFailure) {
        log.error("compensation step failed", compensationFailure);
      }
    }
  }

  @Transactional(readOnly = true)
  public List<Order> history(String customerRef) {
    return orders.findByCustomerRefOrderByCreatedAtDesc(customerRef);
  }

  @Transactional(readOnly = true)
  public Order get(Long id) {
    return orders.findWithLinesById(id).orElseThrow(() -> new OrderNotFoundException(id));
  }

  @Transactional
  public Order advance(Long id, OrderStatus target) {
    Order order = orders.findWithLinesById(id).orElseThrow(() -> new OrderNotFoundException(id));
    order.transitionTo(target);
    return orders.save(order);
  }
}
