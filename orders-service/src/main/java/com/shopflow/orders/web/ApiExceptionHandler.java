package com.shopflow.orders.web;

import com.shopflow.orders.domain.IllegalOrderTransitionException;
import com.shopflow.orders.domain.OrderNotFoundException;
import com.shopflow.orders.domain.OrderPlacementException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<Map<String, Object>> notFound(OrderNotFoundException ex) {
    return body(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(IllegalOrderTransitionException.class)
  public ResponseEntity<Map<String, Object>> conflict(IllegalOrderTransitionException ex) {
    return body(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(OrderPlacementException.class)
  public ResponseEntity<Map<String, Object>> placementFailed(OrderPlacementException ex) {
    return body(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
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
