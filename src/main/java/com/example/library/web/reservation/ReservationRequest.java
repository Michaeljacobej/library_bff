package com.example.library.web.reservation;

import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
    @NotNull Long bookId,
    @NotNull Long memberId
) {}
