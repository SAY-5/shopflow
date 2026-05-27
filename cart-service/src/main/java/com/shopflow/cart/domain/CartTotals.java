package com.shopflow.cart.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Pure computation of cart monetary totals. Kept free of persistence to make it easy to test. */
public final class CartTotals {

  private CartTotals() {}

  public static BigDecimal subtotal(List<CartItem> items) {
    return items.stream()
        .map(CartItem::lineTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(2, RoundingMode.HALF_UP);
  }

  public static int totalQuantity(List<CartItem> items) {
    return items.stream().mapToInt(CartItem::getQuantity).sum();
  }
}
