package com.shopflow.catalog.domain;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {

  private final ProductRepository products;

  public CatalogService(ProductRepository products) {
    this.products = products;
  }

  @Transactional(readOnly = true)
  public List<Product> listProducts() {
    return products.findAll();
  }

  @Transactional(readOnly = true)
  public Product getProduct(Long id) {
    return products.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
  }

  @Transactional
  public void reserve(Long productId, int units) {
    Product product =
        products.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
    product.reserve(units);
    products.save(product);
  }

  @Transactional
  public void release(Long productId, int units) {
    Product product =
        products.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
    product.release(units);
    products.save(product);
  }
}
