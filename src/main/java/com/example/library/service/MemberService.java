package com.example.library.service;

import com.example.library.domain.Member;
import com.example.library.sqladapter.SqlAdapterClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
  private final SqlAdapterClient sqlAdapterClient;

  public MemberService(SqlAdapterClient sqlAdapterClient) {
    this.sqlAdapterClient = sqlAdapterClient;
  }

  public List<Member> list() {
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select id, name, email, role_id from members order by id",
        Map.of()
    );
    return rows.stream().map(MemberService::mapMember).toList();
  }

  public Member get(Long id) {
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select id, name, email, role_id from members where id = :id",
        Map.of("id", id)
    );
    if (rows.isEmpty()) {
      throw new NotFoundException("Member not found");
    }
    return mapMember(rows.get(0));
  }

  public Member create(Member member) {
    if (existsByEmail(member.getEmail())) {
      throw new BusinessRuleException("Email already exists");
    }
    Long roleId = requireRoleId(member.getRoleId());
    Map<String, Object> params = new HashMap<>();
    params.put("name", member.getName());
    params.put("email", member.getEmail());
    params.put("roleId", roleId);

    int rows = sqlAdapterClient.execute(
        "insert into members (name, email, role_id) values (:name, :email, :roleId)",
        params
    );
    if (rows <= 0) {
      throw new BusinessRuleException("Member insert failed");
    }

    return getByEmail(member.getEmail());
  }

  public Member update(Long id, Member update) {
    Member existing = get(id);
    if (!existing.getEmail().equals(update.getEmail()) && existsByEmail(update.getEmail())) {
      throw new BusinessRuleException("Email already exists");
    }
    Long roleId = requireRoleId(update.getRoleId());
    Map<String, Object> params = new HashMap<>();
    params.put("id", id);
    params.put("name", update.getName());
    params.put("email", update.getEmail());
    params.put("roleId", roleId);

    int rows = sqlAdapterClient.execute(
        "update members set name = :name, email = :email, role_id = :roleId where id = :id",
        params
    );
    if (rows <= 0) {
      throw new NotFoundException("Member not found");
    }

    return get(id);
  }

  public void delete(Long id) {
    Map<String, Object> params = new HashMap<>();
    params.put("id", id);
    int rows = sqlAdapterClient.execute("delete from members where id = :id", params);
    if (rows <= 0) {
      throw new NotFoundException("Member not found");
    }
  }

  private boolean existsByEmail(String email) {
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select id from members where email = :email",
        Map.of("email", email)
    );
    return !rows.isEmpty();
  }

  private Member getByEmail(String email) {
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select id, name, email, role_id from members where email = :email",
        Map.of("email", email)
    );
    if (rows.isEmpty()) {
      throw new NotFoundException("Member not found after insert");
    }
    return mapMember(rows.get(0));
  }

  private static Member mapMember(Map<String, Object> row) {
    Member member = new Member();
    member.setId(toLong(row, "id"));
    member.setName(toString(row, "name"));
    member.setEmail(toString(row, "email"));
    member.setRoleId(toLong(row, "role_id"));
    return member;
  }

  private Long requireRoleId(Long roleId) {
    if (roleId == null) {
      throw new BusinessRuleException("roleId is required");
    }
    if (!roleExists(roleId)) {
      throw new BusinessRuleException("Role not found");
    }
    return roleId;
  }

  private boolean roleExists(Long roleId) {
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select id from roles where id = :id",
        Map.of("id", roleId)
    );
    return !rows.isEmpty();
  }

  private static Long toLong(Map<String, Object> row, String key) {
    Object value = getValue(row, key);
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    return Long.parseLong(value.toString());
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
