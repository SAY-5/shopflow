package com.shopflow.cart;

import static org.assertj.core.api.Assertions.assertThat;

import com.shopflow.cart.domain.CartItem;
import com.shopflow.cart.domain.CartTotals;
import java.math.BigDecimal;
import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

class CartTotalsPropertyTest {

  @Provide
  Arbitrary<List<CartItem>> cartItems() {
    Arbitrary<BigDecimal> price =
        Arbitraries.integers()
            .between(1, 100_00)
            .map(cents -> new BigDecimal(cents).movePointLeft(2));
    Arbitrary<Integer> quantity = Arbitraries.integers().between(1, 20);
    Arbitrary<CartItem> item =
        Combinators.combine(price, quantity).as((p, q) -> new CartItem("s", 1L, "item", p, q));
    return item.list().ofMaxSize(10);
  }

  @Property
  void subtotalEqualsSumOfLineTotals(@ForAll("cartItems") List<CartItem> items) {
    BigDecimal expected = BigDecimal.ZERO;
    for (CartItem item : items) {
      expected = expected.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
    }
    assertThat(CartTotals.subtotal(items)).isEqualByComparingTo(expected);
  }

  @Property
  void subtotalIsNeverNegative(@ForAll("cartItems") List<CartItem> items) {
    assertThat(CartTotals.subtotal(items)).isGreaterThanOrEqualTo(BigDecimal.ZERO);
  }

  @Property
  void totalQuantityEqualsSumOfQuantities(@ForAll("cartItems") List<CartItem> items) {
    int expected = items.stream().mapToInt(CartItem::getQuantity).sum();
    assertThat(CartTotals.totalQuantity(items)).isEqualTo(expected);
  }
}
