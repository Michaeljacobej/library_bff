package com.example.library.web.audit;

import com.example.library.service.AuditLogService;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")


public class AuditLogController {
  private final AuditLogService auditLogService;

  public AuditLogController(AuditLogService auditLogService) {
    this.auditLogService = auditLogService;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<AuditLogResponse> search(@RequestParam(required = false) String user,
                                       @RequestParam(required = false) Instant from,
                                       @RequestParam(required = false) Instant to) {
    return auditLogService.search(user, from, to).stream().map(this::toResponse).toList();
  }

  private AuditLogResponse toResponse(Map<String, Object> row) {
    return new AuditLogResponse(
        toLong(row, "id"),
        toString(row, "user_name"),
        toString(row, "sql_text"),
        toInstant(row, "executed_at"),
        toInt(row, "rows_affected"),
        toBoolean(row, "success"),
        toString(row, "error_message")
    );
  }

  private Long toLong(Map<String, Object> row, String key) {
    Object value = getValue(row, key);
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    return Long.parseLong(value.toString());
  }

  private Integer toInt(Map<String, Object> row, String key) {
    Object value = getValue(row, key);
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.intValue();
    }
    return Integer.parseInt(value.toString());
  }

  private Boolean toBoolean(Map<String, Object> row, String key) {
    Object value = getValue(row, key);
    if (value == null) {
      return null;
    }
    if (value instanceof Boolean bool) {
      return bool;
    }
    return Boolean.parseBoolean(value.toString());
  }

  private String toString(Map<String, Object> row, String key) {
    Object value = getValue(row, key);
    return value == null ? null : value.toString();
  }

  private Instant toInstant(Map<String, Object> row, String key) {
    Object value = getValue(row, key);
    if (value == null) {
      return null;
    }
    if (value instanceof Instant instant) {
      return instant;
    }
    if (value instanceof Timestamp timestamp) {
      return timestamp.toInstant();
    }
    return Instant.parse(value.toString());
  }

  private Object getValue(Map<String, Object> row, String key) {
    if (row.containsKey(key)) {
      return row.get(key);
    }
    String lower = key.toLowerCase();
    if (row.containsKey(lower)) {
      return row.get(lower);
    }
    String upper = key.toUpperCase();
    if (row.containsKey(upper)) {
      return row.get(upper);
    }
    return null;
  }
}
