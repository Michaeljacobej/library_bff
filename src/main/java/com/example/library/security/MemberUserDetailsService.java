package com.example.library.security;

import com.example.library.sqladapter.SqlAdapterClient;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MemberUserDetailsService implements UserDetailsService {
  private static final Logger log = LoggerFactory.getLogger(MemberUserDetailsService.class);
  private final SqlAdapterClient sqlAdapterClient;

  public MemberUserDetailsService(SqlAdapterClient sqlAdapterClient) {
    this.sqlAdapterClient = sqlAdapterClient;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    log.debug("Loading user by email: {}", email);
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select m.email, m.password_hash, r.name as role_name "
            + "from members m join roles r on m.role_id = r.id "
            + "where m.email = :email and m.deleted_at is null",
        Map.of("email", email)
    );
    if (rows.isEmpty()) {
      log.warn("User not found: {}", email);
      throw new UsernameNotFoundException("User not found");
    }
    Map<String, Object> row = rows.get(0);
    String passwordHash = toString(row, "password_hash");
    if (!StringUtils.hasText(passwordHash)) {
      log.warn("User has no password hash: {}", email);
      throw new UsernameNotFoundException("User not found");
    }
    String roleName = toString(row, "role_name");
    String role = StringUtils.hasText(roleName) ? roleName : "MEMBER";
    log.debug("Loaded user {} with role {}", email, role);

    return User.withUsername(email)
        .password(passwordHash)
        .roles(role)
        .build();
  }

  private static String toString(Map<String, Object> row, String key) {
    Object value = getValue(row, key);
    return value == null ? null : value.toString();
  }

  private static Object getValue(Map<String, Object> row, String key) {
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
