package com.example.library.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberRequest(
    @NotBlank String name,
    @NotBlank @Email String email
) {}
