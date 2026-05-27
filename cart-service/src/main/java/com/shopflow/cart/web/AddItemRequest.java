package com.shopflow.cart.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AddItemRequest(
    @NotNull Long productId,
    @NotNull String productName,
    @NotNull BigDecimal unitPrice,
    @Min(1) int quantity) {}
