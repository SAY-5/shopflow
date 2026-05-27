package com.shopflow.orders.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

  @EntityGraph(attributePaths = "lines")
  List<Order> findByCustomerRefOrderByCreatedAtDesc(String customerRef);

  @EntityGraph(attributePaths = "lines")
  Optional<Order> findWithLinesById(Long id);
}
