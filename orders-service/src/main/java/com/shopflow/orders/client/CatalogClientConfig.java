package com.shopflow.orders.client;

import java.time.Duration;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CatalogClientConfig {

  @Bean
  public RestClient catalogRestClient(
      RestClient.Builder builder,
      @org.springframework.beans.factory.annotation.Value("${shopflow.catalog.base-url}")
          String baseUrl) {
    var settings =
        ClientHttpRequestFactorySettings.DEFAULTS
            .withConnectTimeout(Duration.ofSeconds(2))
            .withReadTimeout(Duration.ofSeconds(3));
    return builder
        .baseUrl(baseUrl)
        .requestFactory(ClientHttpRequestFactories.get(settings))
        .build();
  }
}
