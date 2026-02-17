package com.example.library.sqladapter;

import java.util.Map;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SqlAdapterHealthIndicator implements HealthIndicator {
  private final RestTemplate restTemplate;
  private final SqlAdapterClientProperties properties;

  public SqlAdapterHealthIndicator(RestTemplateBuilder restTemplateBuilder,
                                   SqlAdapterClientProperties properties) {
    this.restTemplate = restTemplateBuilder.build();
    this.properties = properties;
  }

  @Override
  public Health health() {
    String url = buildUrl("/health");
    try {
      ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
      if (response.getStatusCode().is2xxSuccessful()) {
        Object status = response.getBody() == null ? null : response.getBody().get("status");
        return Health.up().withDetail("status", status == null ? "ok" : status).build();
      }
      return Health.down().withDetail("statusCode", response.getStatusCodeValue()).build();
    } catch (Exception ex) {
      return Health.down(ex).build();
    }
  }

  private String buildUrl(String path) {
    String base = properties.getBaseUrl();
    String basePath = properties.getBasePath();
    return joinUrl(joinUrl(base, basePath), path);
  }

  private String joinUrl(String left, String right) {
    if (left == null || left.isBlank()) {
      return right == null ? "" : right;
    }
    if (right == null || right.isBlank()) {
      return left;
    }
    boolean leftSlash = left.endsWith("/");
    boolean rightSlash = right.startsWith("/");
    if (leftSlash && rightSlash) {
      return left + right.substring(1);
    }
    if (!leftSlash && !rightSlash) {
      return left + "/" + right;
    }
    return left + right;
  }
}
