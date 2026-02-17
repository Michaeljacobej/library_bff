package com.example.library.web.audit;

import java.time.Instant;

public record AuditLogResponse(
    Long id,
    String userName,
    String sqlText,
    Instant executedAt,
    Integer rowsAffected,
    Boolean success,
    String errorMessage
) {}
