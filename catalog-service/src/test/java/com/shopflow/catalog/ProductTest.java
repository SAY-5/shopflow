package com.shopflow.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shopflow.catalog.domain.InsufficientInventoryException;
import com.shopflow.catalog.domain.Product;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductTest {

  private Product product() {
    return new Product(1L, "Mug", "A mug", new BigDecimal("18.00"), 10);
  }

  @Test
  void reserveReducesFreeUnits() {
    Product product = product();
    product.reserve(3);
    assertThat(product.freeUnits()).isEqualTo(7);
    assertThat(product.getReservedUnits()).isEqualTo(3);
  }

  @Test
  void reserveBeyondFreeUnitsIsRejected() {
    Product product = product();
    assertThatThrownBy(() -> product.reserve(11))
        .isInstanceOf(InsufficientInventoryException.class);
  }

  @Test
  void releaseReturnsUnitsToFreePool() {
    Product product = product();
    product.reserve(4);
    product.release(4);
    assertThat(product.freeUnits()).isEqualTo(10);
  }

  @Test
  void releaseNeverDrivesReservedNegative() {
    Product product = product();
    product.reserve(2);
    product.release(5);
    assertThat(product.getReservedUnits()).isEqualTo(0);
  }
}
