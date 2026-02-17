package com.example.library.sqladapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HttpSqlAdapterClient implements SqlAdapterClient {
  private final RestTemplate restTemplate;
  private final SqlAdapterClientProperties properties;

  public HttpSqlAdapterClient(RestTemplateBuilder restTemplateBuilder,
                              SqlAdapterClientProperties properties) {
    this.restTemplate = restTemplateBuilder.build();
    this.properties = properties;
  }

  @Override
  public int execute(String sql, Map<String, Object> params) {
    String url = properties.getBaseUrl() + "/execute";
    SqlAdapterExecuteRequest request = new SqlAdapterExecuteRequest(sql, params);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("X-User", actor());

    HttpEntity<SqlAdapterExecuteRequest> entity = new HttpEntity<>(request, headers);
    SqlAdapterExecuteResponse response = restTemplate.postForObject(url, entity, SqlAdapterExecuteResponse.class);

    return Optional.ofNullable(response)
        .map(SqlAdapterExecuteResponse::getRowsAffected)
        .orElse(0);
  }

  @Override
  public List<Map<String, Object>> query(String sql, Map<String, Object> params) {
    String url = properties.getBaseUrl() + "/query";
    SqlAdapterQueryRequest request = new SqlAdapterQueryRequest(sql, params);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("X-User", actor());

    HttpEntity<SqlAdapterQueryRequest> entity = new HttpEntity<>(request, headers);
    ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
        url,
        HttpMethod.POST,
        entity,
        new ParameterizedTypeReference<>() {}
    );

    return Optional.ofNullable(response.getBody()).orElseGet(List::of);
  }

  private String actor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return "anonymous";
    }
    return authentication.getName();
  }
}
