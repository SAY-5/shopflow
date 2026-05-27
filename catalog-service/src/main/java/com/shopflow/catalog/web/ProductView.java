package com.shopflow.catalog.web;

import com.shopflow.catalog.domain.Product;
import java.math.BigDecimal;

public record ProductView(
    Long id, String name, String description, BigDecimal price, int availableUnits) {

  public static ProductView from(Product product) {
    return new ProductView(
        product.getId(),
        product.getName(),
        product.getDescription(),
        product.getPrice(),
        product.freeUnits());
  }
}
