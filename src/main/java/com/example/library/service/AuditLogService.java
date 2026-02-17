package com.example.library.service;

import com.example.library.sqladapter.SqlAdapterClient;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {
  private final SqlAdapterClient sqlAdapterClient;

  public AuditLogService(SqlAdapterClient sqlAdapterClient) {
    this.sqlAdapterClient = sqlAdapterClient;
  }

  public List<Map<String, Object>> search(String user, Instant from, Instant to) {
    StringBuilder sql = new StringBuilder(
        "select id, user_name, sql_text, executed_at, rows_affected, success, error_message "
            + "from audit_log where 1=1");
    Map<String, Object> params = new HashMap<>();

    if (user != null && !user.isBlank()) {
      sql.append(" and user_name = :user");
      params.put("user", user);
    }
    if (from != null) {
      sql.append(" and executed_at >= :from");
      params.put("from", from);
    }
    if (to != null) {
      sql.append(" and executed_at <= :to");
      params.put("to", to);
    }

    sql.append(" order by executed_at desc");
    return sqlAdapterClient.query(sql.toString(), params);
  }
}
