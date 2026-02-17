package com.example.library.service;

import com.example.library.sqladapter.SqlAdapterClient;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {
  private final SqlAdapterClient sqlAdapterClient;

  public ReservationService(SqlAdapterClient sqlAdapterClient) {
    this.sqlAdapterClient = sqlAdapterClient;
  }

  public List<Map<String, Object>> list() {
    return sqlAdapterClient.query(
        "select id, book_id, member_id, role_name, status, created_at, fulfilled_at, canceled_at "
            + "from reservations order by created_at desc",
        Map.of()
    );
  }

  public Map<String, Object> get(Long id) {
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select id, book_id, member_id, role_name, status, created_at, fulfilled_at, canceled_at "
            + "from reservations where id = :id",
        Map.of("id", id)
    );
    if (rows.isEmpty()) {
      throw new NotFoundException("Reservation not found");
    }
    return rows.get(0);
  }

  public Map<String, Object> create(Long bookId, Long memberId, String roleName) {
    Instant now = Instant.now();
    Map<String, Object> params = new HashMap<>();
    params.put("bookId", bookId);
    params.put("memberId", memberId);
    params.put("roleName", roleName);
    params.put("status", "PENDING");
    params.put("createdAt", now);

    int rows = sqlAdapterClient.execute(
        "insert into reservations (book_id, member_id, role_name, status, created_at) "
            + "values (:bookId, :memberId, :roleName, :status, :createdAt)",
        params
    );
    if (rows <= 0) {
      throw new BusinessRuleException("Reservation insert failed");
    }

    List<Map<String, Object>> result = sqlAdapterClient.query(
        "select id, book_id, member_id, role_name, status, created_at, fulfilled_at, canceled_at "
            + "from reservations where book_id = :bookId and member_id = :memberId and created_at = :createdAt",
        Map.of("bookId", bookId, "memberId", memberId, "createdAt", now)
    );
    if (result.isEmpty()) {
      throw new BusinessRuleException("Reservation not found after insert");
    }
    return result.get(0);
  }

  public void cancel(Long id) {
    Map<String, Object> params = new HashMap<>();
    params.put("id", id);
    params.put("canceledAt", Instant.now());
    int rows = sqlAdapterClient.execute(
        "update reservations set status = 'CANCELED', canceled_at = :canceledAt "
            + "where id = :id and status = 'PENDING'",
        params
    );
    if (rows <= 0) {
      throw new BusinessRuleException("Reservation not found or already processed");
    }
  }

  public String resolveRoleNameForMember(Long memberId) {
    List<Map<String, Object>> memberRows = sqlAdapterClient.query(
        "select role_id from members where id = :id and deleted_at is null",
        Map.of("id", memberId)
    );
    if (memberRows.isEmpty()) {
      return "MEMBER";
    }
    Object roleId = memberRows.get(0).get("role_id");
    if (roleId == null) {
      return "MEMBER";
    }

    List<Map<String, Object>> roleRows = sqlAdapterClient.query(
        "select name from roles where id = :id",
        Map.of("id", roleId)
    );
    if (roleRows.isEmpty()) {
      return "MEMBER";
    }
    Object name = roleRows.get(0).get("name");
    return name == null ? "MEMBER" : name.toString();
  }

  public Long toLong(Map<String, Object> row, String key) {
    Object value = getValue(row, key);
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    return Long.parseLong(value.toString());
  }

  public String toString(Map<String, Object> row, String key) {
    Object value = getValue(row, key);
    return value == null ? null : value.toString();
  }

  public Instant toInstant(Map<String, Object> row, String key) {
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
