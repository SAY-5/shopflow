package com.shopflow.orders.domain;

import java.math.BigDecimal;
import java.util.List;

public record PlaceOrderCommand(String customerRef, List<Line> lines) {

  public record Line(Long productId, String productName, BigDecimal unitPrice, int quantity) {}
}
