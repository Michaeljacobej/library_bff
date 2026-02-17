package com.example.library.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {
  private String secret = "abcde1234567890";
  private Duration expiration = Duration.ofMinutes(2);
  private String issuer = "library-bff";

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public Duration getExpiration() {
    return expiration;
  }

  public void setExpiration(Duration expiration) {
    this.expiration = expiration;
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }
}
