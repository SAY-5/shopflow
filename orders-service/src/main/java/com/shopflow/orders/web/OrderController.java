package com.shopflow.orders.web;

import com.shopflow.orders.domain.Order;
import com.shopflow.orders.domain.OrderService;
import com.shopflow.orders.domain.OrderStatus;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

  private final OrderService orders;

  public OrderController(OrderService orders) {
    this.orders = orders;
  }

  @PostMapping
  public ResponseEntity<OrderView> place(@Valid @RequestBody PlaceOrderRequest request) {
    Order order = orders.placeOrder(request.toCommand());
    return ResponseEntity.status(HttpStatus.CREATED).body(OrderView.from(order));
  }

  @GetMapping
  public List<OrderView> history(@RequestParam String customerRef) {
    return orders.history(customerRef).stream().map(OrderView::from).toList();
  }

  @GetMapping("/{id}")
  public OrderView get(@PathVariable Long id) {
    return OrderView.from(orders.get(id));
  }

  @PostMapping("/{id}/transitions")
  public OrderView transition(@PathVariable Long id, @RequestParam OrderStatus to) {
    return OrderView.from(orders.advance(id, to));
  }
}
