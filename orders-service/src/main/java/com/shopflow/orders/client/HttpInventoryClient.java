package com.shopflow.orders.client;

import java.util.Map;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpInventoryClient implements InventoryClient {

  private final RestClient restClient;

  public HttpInventoryClient(RestClient catalogRestClient) {
    this.restClient = catalogRestClient;
  }

  @Override
  public void reserve(Long productId, int units) {
    call("/products/reservations", productId, units);
  }

  @Override
  public void release(Long productId, int units) {
    call("/products/reservations/release", productId, units);
  }

  private void call(String path, Long productId, int units) {
    restClient
        .post()
        .uri(path)
        .body(Map.of("productId", productId, "units", units))
        .retrieve()
        .onStatus(
            HttpStatusCode::isError,
            (request, response) -> {
              throw new InventoryException(
                  "catalog returned " + response.getStatusCode() + " for " + path);
            })
        .toBodilessEntity();
  }
}
