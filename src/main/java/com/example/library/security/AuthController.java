package com.example.library.security;

import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final RoleLookupService roleLookupService;

  public AuthController(AuthenticationManager authenticationManager,
                        JwtService jwtService,
                        RoleLookupService roleLookupService) {
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.roleLookupService = roleLookupService;
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody AuthRequest request) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.username(), request.password())
    );
    String subject = authentication.getName();
    var dbRoles = roleLookupService.resolveRolesByEmail(subject);
    String token = dbRoles.isEmpty()
        ? jwtService.generateToken(authentication)
        : jwtService.generateToken(subject, dbRoles);
    return new AuthResponse(token, "Bearer");
  }
}
