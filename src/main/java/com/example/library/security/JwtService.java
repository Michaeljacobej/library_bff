package com.example.library.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final JwtProperties properties;

  public JwtService(JwtProperties properties) {
    this.properties = properties;
  }

  public String generateToken(Authentication authentication) {
    List<String> roles = authentication.getAuthorities().stream()
      .map(GrantedAuthority::getAuthority)
      .collect(Collectors.toList());
    return generateToken(authentication.getName(), roles);
  }

    public String generateToken(String subject, List<String> roles) {
    Instant now = Instant.now();
    Instant expiresAt = now.plus(properties.getExpiration());

    return Jwts.builder()
      .setIssuer(properties.getIssuer())
      .setSubject(subject)
      .setIssuedAt(Date.from(now))
      .setExpiration(Date.from(expiresAt))
      .claim("roles", roles)
      .signWith(signingKey(), SignatureAlgorithm.HS256)
      .compact();
    }

  public Claims parseToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(signingKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private SecretKey signingKey() {
    byte[] keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
