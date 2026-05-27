package com.shopflow.catalog.web;

import com.shopflow.catalog.domain.InsufficientInventoryException;
import com.shopflow.catalog.domain.ProductNotFoundException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<Map<String, Object>> notFound(ProductNotFoundException ex) {
    return body(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(InsufficientInventoryException.class)
  public ResponseEntity<Map<String, Object>> conflict(InsufficientInventoryException ex) {
    return body(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException ex) {
    return body(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message) {
    return ResponseEntity.status(status)
        .body(
            Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message));
  }
}
