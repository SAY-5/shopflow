package com.shopflow.cart.domain;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

  private final CartItemRepository items;

  public CartService(CartItemRepository items) {
    this.items = items;
  }

  @Transactional(readOnly = true)
  public List<CartItem> itemsFor(String sessionId) {
    return items.findBySessionId(sessionId);
  }

  @Transactional
  public CartItem addItem(
      String sessionId, Long productId, String productName, BigDecimal unitPrice, int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("quantity must be positive");
    }
    return items
        .findBySessionIdAndProductId(sessionId, productId)
        .map(
            existing -> {
              existing.addQuantity(quantity);
              return items.save(existing);
            })
        .orElseGet(
            () -> items.save(new CartItem(sessionId, productId, productName, unitPrice, quantity)));
  }

  @Transactional
  public void updateQuantity(String sessionId, Long productId, int quantity) {
    CartItem item =
        items
            .findBySessionIdAndProductId(sessionId, productId)
            .orElseThrow(() -> new CartItemNotFoundException(sessionId, productId));
    if (quantity <= 0) {
      items.delete(item);
      return;
    }
    item.setQuantity(quantity);
    items.save(item);
  }

  @Transactional
  public void removeItem(String sessionId, Long productId) {
    items.findBySessionIdAndProductId(sessionId, productId).ifPresent(items::delete);
  }

  @Transactional
  public void clear(String sessionId) {
    items.deleteBySessionId(sessionId);
  }
}
