package com.shopflow.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;

@Entity
@Table(name = "product")
public class Product {

  @Id private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private BigDecimal price;

  @Column(name = "available_units", nullable = false)
  private int availableUnits;

  @Column(name = "reserved_units", nullable = false)
  private int reservedUnits;

  @Version private long version;

  protected Product() {}

  public Product(Long id, String name, String description, BigDecimal price, int availableUnits) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.price = price;
    this.availableUnits = availableUnits;
    this.reservedUnits = 0;
  }

  public int freeUnits() {
    return availableUnits - reservedUnits;
  }

  public void reserve(int units) {
    if (units <= 0) {
      throw new IllegalArgumentException("reservation units must be positive");
    }
    if (units > freeUnits()) {
      throw new InsufficientInventoryException(id, units, freeUnits());
    }
    reservedUnits += units;
  }

  public void release(int units) {
    if (units <= 0) {
      throw new IllegalArgumentException("release units must be positive");
    }
    reservedUnits = Math.max(0, reservedUnits - units);
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public int getAvailableUnits() {
    return availableUnits;
  }

  public int getReservedUnits() {
    return reservedUnits;
  }
}
