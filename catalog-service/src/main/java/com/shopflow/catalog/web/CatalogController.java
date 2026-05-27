package com.shopflow.catalog.web;

import com.shopflow.catalog.domain.CatalogService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class CatalogController {

  private final CatalogService catalog;

  public CatalogController(CatalogService catalog) {
    this.catalog = catalog;
  }

  @GetMapping
  public List<ProductView> list() {
    return catalog.listProducts().stream().map(ProductView::from).toList();
  }

  @GetMapping("/{id}")
  public ProductView get(@PathVariable Long id) {
    return ProductView.from(catalog.getProduct(id));
  }

  @PostMapping("/reservations")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void reserve(@Valid @RequestBody ReservationRequest request) {
    catalog.reserve(request.productId(), request.units());
  }

  @PostMapping("/reservations/release")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void release(@Valid @RequestBody ReservationRequest request) {
    catalog.release(request.productId(), request.units());
  }
}
