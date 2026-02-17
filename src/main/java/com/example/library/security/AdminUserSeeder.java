package com.example.library.security;

import com.example.library.config.AdminUserProperties;
import com.example.library.sqladapter.SqlAdapterClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AdminUserSeeder implements ApplicationRunner {
  private final SqlAdapterClient sqlAdapterClient;
  private final PasswordEncoder passwordEncoder;
  private final AdminUserProperties adminUserProperties;

  public AdminUserSeeder(SqlAdapterClient sqlAdapterClient,
                         PasswordEncoder passwordEncoder,
                         AdminUserProperties adminUserProperties) {
    this.sqlAdapterClient = sqlAdapterClient;
    this.passwordEncoder = passwordEncoder;
    this.adminUserProperties = adminUserProperties;
  }

  @Override
  public void run(ApplicationArguments args) {
    try {
      if (!StringUtils.hasText(adminUserProperties.getEmail())
          || !StringUtils.hasText(adminUserProperties.getPassword())) {
        return;
      }
      Long roleId = resolveAdminRoleId();
      if (roleId == null) {
        System.err.println("WARNING: ADMIN role missing, skipping admin user seeding");
        return;
      }
    Map<String, Object> existing = findMemberByEmail(adminUserProperties.getEmail());
    String passwordHash = passwordEncoder.encode(adminUserProperties.getPassword());
    if (existing != null) {
      // Always update password hash to ensure it matches config
      Map<String, Object> params = new HashMap<>();
      params.put("id", toLong(existing.get("id")));
      params.put("passwordHash", passwordHash);
      sqlAdapterClient.execute(
          "update members set password_hash = :passwordHash where id = :id",
          params
      );
      return;
    }

    Map<String, Object> params = new HashMap<>();
    params.put("name", defaultIfBlank(adminUserProperties.getName(), "Admin"));
    params.put("email", adminUserProperties.getEmail());
    params.put("roleId", roleId);
    params.put("passwordHash", passwordHash);

    sqlAdapterClient.execute(
        "insert into members (name, email, role_id, password_hash) "
            + "values (:name, :email, :roleId, :passwordHash)",
        params
    );
    } catch (Exception e) {
      System.err.println("WARNING: Failed to seed admin user: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private Long resolveAdminRoleId() {
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select id from roles where name = :name",
        Map.of("name", "ADMIN")
    );
    if (rows.isEmpty()) {
      return null;
    }
    Object value = rows.get(0).get("id");
    if (value instanceof Number number) {
      return number.longValue();
    }
    return value == null ? null : Long.parseLong(value.toString());
  }

  private Map<String, Object> findMemberByEmail(String email) {
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select id, password_hash from members where email = :email and deleted_at is null",
        Map.of("email", email)
    );
    return rows.isEmpty() ? null : rows.get(0);
  }

  private Long toLong(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    return Long.parseLong(value.toString());
  }

  private String toString(Map<String, Object> row, String key) {
    Object value = row.get(key);
    return value == null ? null : value.toString();
  }

  private String defaultIfBlank(String value, String fallback) {
    return StringUtils.hasText(value) ? value : fallback;
  }
}
