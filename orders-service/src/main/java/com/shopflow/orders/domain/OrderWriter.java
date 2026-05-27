package com.shopflow.orders.domain;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Persists an order in its own transaction, separate from the saga's inventory calls. */
@Component
public class OrderWriter {

  private final OrderRepository orders;

  public OrderWriter(OrderRepository orders) {
    this.orders = orders;
  }

  @Transactional
  public Order persist(PlaceOrderCommand command) {
    List<OrderLine> lines =
        command.lines().stream()
            .map(l -> new OrderLine(l.productId(), l.productName(), l.unitPrice(), l.quantity()))
            .toList();
    return orders.save(new Order(command.customerRef(), lines));
  }
}
