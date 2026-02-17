package com.example.library.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberRequest(
    @NotBlank String name,
    @NotBlank @Email String email,
    @NotNull Long roleId,
    String password
) {}
