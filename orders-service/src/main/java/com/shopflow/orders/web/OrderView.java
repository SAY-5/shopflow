package com.shopflow.orders.web;

import com.shopflow.orders.domain.Order;
import com.shopflow.orders.domain.OrderLine;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderView(
    Long id,
    String customerRef,
    String status,
    Instant createdAt,
    BigDecimal total,
    List<OrderLineView> lines) {

  public static OrderView from(Order order) {
    return new OrderView(
        order.getId(),
        order.getCustomerRef(),
        order.getStatus().name(),
        order.getCreatedAt(),
        order.total(),
        order.getLines().stream().map(OrderLineView::from).toList());
  }

  public record OrderLineView(
      Long productId, String productName, BigDecimal unitPrice, int quantity) {

    static OrderLineView from(OrderLine line) {
      return new OrderLineView(
          line.getProductId(), line.getProductName(), line.getUnitPrice(), line.getQuantity());
    }
  }
}
