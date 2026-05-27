package com.shopflow.orders.web;

import com.shopflow.orders.domain.PlaceOrderCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record PlaceOrderRequest(@NotNull String customerRef, @NotEmpty @Valid List<Line> lines) {

  public PlaceOrderCommand toCommand() {
    return new PlaceOrderCommand(
        customerRef,
        lines.stream()
            .map(
                l ->
                    new PlaceOrderCommand.Line(
                        l.productId(), l.productName(), l.unitPrice(), l.quantity()))
            .toList());
  }

  public record Line(
      @NotNull Long productId,
      @NotNull String productName,
      @NotNull BigDecimal unitPrice,
      @Min(1) int quantity) {}
}
