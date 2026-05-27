package com.shopflow.cart;

import static org.assertj.core.api.Assertions.assertThat;

import com.shopflow.cart.web.AddItemRequest;
import com.shopflow.cart.web.CartView;
import com.shopflow.cart.web.UpdateQuantityRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

class CartApiIT extends PostgresIntegrationTest {

  @Autowired private TestRestTemplate rest;

  @Test
  void addingItemsBuildsCartWithSubtotal() {
    String session = "session-add";
    rest.postForObject(
        "/carts/" + session + "/items",
        new AddItemRequest(1L, "Mug", new BigDecimal("18.00"), 2),
        CartView.class);
    CartView view =
        rest.postForObject(
            "/carts/" + session + "/items",
            new AddItemRequest(2L, "Tote", new BigDecimal("24.00"), 1),
            CartView.class);
    assertThat(view.totalQuantity()).isEqualTo(3);
    assertThat(view.subtotal()).isEqualByComparingTo("60.00");
  }

  @Test
  void addingSameProductAccumulatesQuantity() {
    String session = "session-accumulate";
    rest.postForObject(
        "/carts/" + session + "/items",
        new AddItemRequest(1L, "Mug", new BigDecimal("18.00"), 1),
        CartView.class);
    CartView view =
        rest.postForObject(
            "/carts/" + session + "/items",
            new AddItemRequest(1L, "Mug", new BigDecimal("18.00"), 2),
            CartView.class);
    assertThat(view.items()).hasSize(1);
    assertThat(view.items().get(0).quantity()).isEqualTo(3);
  }

  @Test
  void updatingQuantityToZeroRemovesLine() {
    String session = "session-zero";
    rest.postForObject(
        "/carts/" + session + "/items",
        new AddItemRequest(1L, "Mug", new BigDecimal("18.00"), 2),
        CartView.class);
    CartView view =
        rest.exchange(
                "/carts/" + session + "/items/1",
                HttpMethod.PUT,
                new HttpEntity<>(new UpdateQuantityRequest(0)),
                CartView.class)
            .getBody();
    assertThat(view.items()).isEmpty();
  }
}
