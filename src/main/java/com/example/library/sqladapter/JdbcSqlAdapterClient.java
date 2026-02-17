package com.example.library.sqladapter;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class JdbcSqlAdapterClient implements SqlAdapterClient {
  private final JdbcTemplate jdbcTemplate;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public JdbcSqlAdapterClient(JdbcTemplate jdbcTemplate,
                              NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  @Override
  public int execute(String sql, Map<String, Object> params) {
    try {
      int rows = namedParameterJdbcTemplate.update(sql, params);
      audit(actor(), sql, rows, true, null);
      return rows;
    } catch (RuntimeException ex) {
      audit(actor(), sql, null, false, ex.getMessage());
      throw ex;
    }
  }

  @Override
  public List<Map<String, Object>> query(String sql, Map<String, Object> params) {
    try {
      List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, params);
      audit(actor(), sql, rows.size(), true, null);
      return rows;
    } catch (RuntimeException ex) {
      audit(actor(), sql, null, false, ex.getMessage());
      throw ex;
    }
  }

  private void audit(String actor, String sql, Integer rows, boolean success, String error) {
    try {
      jdbcTemplate.update(
          "insert into audit_log (user_name, sql_text, executed_at, rows_affected, success, error_message) "
              + "values (?, ?, ?, ?, ?, ?)",
          actor,
          sql,
          Timestamp.from(Instant.now()),
          rows,
          success,
          error
      );
    } catch (RuntimeException ignored) {
      
    }
  }

  private String actor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return "anonymous";
    }
    return authentication.getName();
  }
}
