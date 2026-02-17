package com.example.library.web.dto;

import java.time.Instant;

public record LoanResponse(
    Long id,
    Long bookId,
    Long memberId,
    Instant borrowedAt,
    Instant dueDate,
    Instant returnedAt
) {}
