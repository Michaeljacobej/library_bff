package com.example.library.security;

import com.example.library.sqladapter.SqlAdapterClient;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RoleLookupService {
  private final SqlAdapterClient sqlAdapterClient;

  public RoleLookupService(SqlAdapterClient sqlAdapterClient) {
    this.sqlAdapterClient = sqlAdapterClient;
  }

  public List<String> resolveRolesByEmail(String email) {
    List<Map<String, Object>> memberRows = sqlAdapterClient.query(
        "select role_id from members where email = :email",
        Map.of("email", email)
    );
    if (memberRows.isEmpty()) {
      return List.of();
    }
    Object roleId = memberRows.get(0).get("role_id");
    if (roleId == null) {
      return List.of();
    }

    List<Map<String, Object>> roleRows = sqlAdapterClient.query(
        "select name from roles where id = :id",
        Map.of("id", roleId)
    );
    if (roleRows.isEmpty()) {
      return List.of();
    }
    Object name = roleRows.get(0).get("name");
    if (name == null) {
      return List.of();
    }
    return List.of("ROLE_" + name.toString());
  }
}
