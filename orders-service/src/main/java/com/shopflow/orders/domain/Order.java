package com.shopflow.orders.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_order")
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "customer_ref", nullable = false)
  private String customerRef;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "order_id", nullable = false)
  private List<OrderLine> lines = new ArrayList<>();

  protected Order() {}

  public Order(String customerRef, List<OrderLine> lines) {
    if (lines == null || lines.isEmpty()) {
      throw new IllegalArgumentException("an order needs at least one line");
    }
    this.customerRef = customerRef;
    this.status = OrderStatus.PENDING;
    this.createdAt = Instant.now();
    this.lines = new ArrayList<>(lines);
  }

  public BigDecimal total() {
    return lines.stream()
        .map(OrderLine::lineTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(2, RoundingMode.HALF_UP);
  }

  public void transitionTo(OrderStatus target) {
    if (!status.canTransitionTo(target)) {
      throw new IllegalOrderTransitionException(status, target);
    }
    this.status = target;
  }

  public Long getId() {
    return id;
  }

  public String getCustomerRef() {
    return customerRef;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public List<OrderLine> getLines() {
    return List.copyOf(lines);
  }
}
