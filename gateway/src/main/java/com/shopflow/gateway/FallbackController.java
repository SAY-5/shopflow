package com.shopflow.gateway;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Returns a degraded response when a downstream service is unavailable and its circuit breaker
 * routes the call here. The gateway answers quickly with 503 rather than holding the request open
 * or surfacing the downstream failure.
 */
@RestController
public class FallbackController {

  @RequestMapping(
      value = "/fallback/{service}",
      method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
  public ResponseEntity<Map<String, Object>> fallback(@PathVariable String service) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(
            Map.of(
                "service",
                service,
                "status",
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "message",
                service + " is unavailable, please retry shortly"));
  }
}
