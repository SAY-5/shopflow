package com.shopflow.cart.web;

import com.shopflow.cart.domain.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/carts/{sessionId}")
public class CartController {

  private final CartService carts;

  public CartController(CartService carts) {
    this.carts = carts;
  }

  @GetMapping
  public CartView view(@PathVariable String sessionId) {
    return CartView.from(sessionId, carts.itemsFor(sessionId));
  }

  @PostMapping("/items")
  public CartView add(@PathVariable String sessionId, @Valid @RequestBody AddItemRequest request) {
    carts.addItem(
        sessionId,
        request.productId(),
        request.productName(),
        request.unitPrice(),
        request.quantity());
    return CartView.from(sessionId, carts.itemsFor(sessionId));
  }

  @PutMapping("/items/{productId}")
  public CartView update(
      @PathVariable String sessionId,
      @PathVariable Long productId,
      @Valid @RequestBody UpdateQuantityRequest request) {
    carts.updateQuantity(sessionId, productId, request.quantity());
    return CartView.from(sessionId, carts.itemsFor(sessionId));
  }

  @DeleteMapping("/items/{productId}")
  public CartView remove(@PathVariable String sessionId, @PathVariable Long productId) {
    carts.removeItem(sessionId, productId);
    return CartView.from(sessionId, carts.itemsFor(sessionId));
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void clear(@PathVariable String sessionId) {
    carts.clear(sessionId);
  }
}
