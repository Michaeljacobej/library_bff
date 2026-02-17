package com.example.library.security;

import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;

  public JwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.substring("Bearer ".length());
    try {
      Claims claims = jwtService.parseToken(token);
      String subject = claims.getSubject();
      @SuppressWarnings("unchecked")
      List<String> roles = claims.get("roles", List.class);
      List<SimpleGrantedAuthority> authorities = roles == null ? List.of() :
          roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(subject, null, authorities);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (Exception ex) {
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }
}
