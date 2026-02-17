package com.example.library.web.reservation;

import java.time.Instant;

public record ReservationResponse(
    Long id,
    Long bookId,
    Long memberId,
    String roleName,
    String status,
    Instant createdAt,
    Instant fulfilledAt,
    Instant canceledAt
) {}
