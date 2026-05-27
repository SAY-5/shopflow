package com.shopflow.cart.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_item")
public class CartItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_id", nullable = false)
  private String sessionId;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "product_name", nullable = false)
  private String productName;

  @Column(name = "unit_price", nullable = false)
  private BigDecimal unitPrice;

  @Column(nullable = false)
  private int quantity;

  protected CartItem() {}

  public CartItem(
      String sessionId, Long productId, String productName, BigDecimal unitPrice, int quantity) {
    this.sessionId = sessionId;
    this.productId = productId;
    this.productName = productName;
    this.unitPrice = unitPrice;
    this.quantity = quantity;
  }

  public BigDecimal lineTotal() {
    return unitPrice.multiply(BigDecimal.valueOf(quantity));
  }

  public void addQuantity(int delta) {
    this.quantity += delta;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public Long getId() {
    return id;
  }

  public String getSessionId() {
    return sessionId;
  }

  public Long getProductId() {
    return productId;
  }

  public String getProductName() {
    return productName;
  }

  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

  public int getQuantity() {
    return quantity;
  }
}
