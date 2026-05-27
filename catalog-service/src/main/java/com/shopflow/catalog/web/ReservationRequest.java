package com.shopflow.catalog.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReservationRequest(@NotNull Long productId, @Min(1) int units) {}
