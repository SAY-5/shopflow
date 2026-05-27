package com.shopflow.cart.web;

import com.shopflow.cart.domain.CartItem;
import com.shopflow.cart.domain.CartTotals;
import java.math.BigDecimal;
import java.util.List;

public record CartView(
    String sessionId, List<CartLineView> items, BigDecimal subtotal, int totalQuantity) {

  public static CartView from(String sessionId, List<CartItem> items) {
    List<CartLineView> lines = items.stream().map(CartLineView::from).toList();
    return new CartView(
        sessionId, lines, CartTotals.subtotal(items), CartTotals.totalQuantity(items));
  }

  public record CartLineView(
      Long productId,
      String productName,
      BigDecimal unitPrice,
      int quantity,
      BigDecimal lineTotal) {

    static CartLineView from(CartItem item) {
      return new CartLineView(
          item.getProductId(),
          item.getProductName(),
          item.getUnitPrice(),
          item.getQuantity(),
          item.lineTotal());
    }
  }
}
