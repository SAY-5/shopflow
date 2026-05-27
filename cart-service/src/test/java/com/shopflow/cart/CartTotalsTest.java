package com.shopflow.cart;

import static org.assertj.core.api.Assertions.assertThat;

import com.shopflow.cart.domain.CartItem;
import com.shopflow.cart.domain.CartTotals;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CartTotalsTest {

  private CartItem item(String name, String price, int quantity) {
    return new CartItem("s1", 1L, name, new BigDecimal(price), quantity);
  }

  @Test
  void subtotalSumsLineTotals() {
    List<CartItem> items = List.of(item("a", "10.00", 2), item("b", "3.50", 4));
    assertThat(CartTotals.subtotal(items)).isEqualByComparingTo("34.00");
  }

  @Test
  void subtotalOfEmptyCartIsZero() {
    assertThat(CartTotals.subtotal(List.of())).isEqualByComparingTo("0.00");
  }

  @Test
  void totalQuantitySumsQuantities() {
    List<CartItem> items = List.of(item("a", "1.00", 2), item("b", "1.00", 5));
    assertThat(CartTotals.totalQuantity(items)).isEqualTo(7);
  }
}
