package com.example.library.security;

public record AuthResponse(
    String token,
    String tokenType
) {}
