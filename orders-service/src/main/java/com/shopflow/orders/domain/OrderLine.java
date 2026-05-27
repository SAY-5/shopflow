package com.shopflow.orders.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "order_line")
public class OrderLine {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "product_name", nullable = false)
  private String productName;

  @Column(name = "unit_price", nullable = false)
  private BigDecimal unitPrice;

  @Column(nullable = false)
  private int quantity;

  protected OrderLine() {}

  public OrderLine(Long productId, String productName, BigDecimal unitPrice, int quantity) {
    this.productId = productId;
    this.productName = productName;
    this.unitPrice = unitPrice;
    this.quantity = quantity;
  }

  public BigDecimal lineTotal() {
    return unitPrice.multiply(BigDecimal.valueOf(quantity));
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
