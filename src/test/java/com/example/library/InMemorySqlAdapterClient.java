package com.example.library;

import com.example.library.sqladapter.SqlAdapterClient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class InMemorySqlAdapterClient implements SqlAdapterClient {
  private long bookSeq = 1L;
  private long memberSeq = 1L;
  private long loanSeq = 1L;
  private long roleSeq = 1L;

  private final Map<Long, Map<String, Object>> books = new HashMap<>();
  private final Map<Long, Map<String, Object>> members = new HashMap<>();
  private final Map<Long, Map<String, Object>> loans = new HashMap<>();
  private final Map<Long, Map<String, Object>> roles = new HashMap<>();

  InMemorySqlAdapterClient() {
    insertRole("ADMIN");
    insertRole("LIBRARIAN");
    insertRole("MEMBER");
  }

  @Override
  public int execute(String sql, Map<String, Object> params) {
    String normalized = normalize(sql);

    if (normalized.startsWith("insert into books")) {
      long id = bookSeq++;
      Map<String, Object> row = new HashMap<>();
      row.put("id", id);
      row.put("title", params.get("title"));
      row.put("author", params.get("author"));
      row.put("isbn", params.get("isbn"));
      row.put("total_copies", params.get("totalCopies"));
      row.put("available_copies", params.get("availableCopies"));
      books.put(id, row);
      return 1;
    }

    if (normalized.startsWith("update books set title")) {
      Long id = toLong(params.get("id"));
      Map<String, Object> row = books.get(id);
      if (row == null) {
        return 0;
      }
      row.put("title", params.get("title"));
      row.put("author", params.get("author"));
      row.put("isbn", params.get("isbn"));
      row.put("total_copies", params.get("totalCopies"));
      row.put("available_copies", params.get("availableCopies"));
      return 1;
    }

    if (normalized.startsWith("delete from books")) {
      Long id = toLong(params.get("id"));
      return books.remove(id) == null ? 0 : 1;
    }

    if (normalized.startsWith("update books set available_copies = available_copies - 1")) {
      Long id = toLong(params.get("id"));
      Map<String, Object> row = books.get(id);
      if (row == null) {
        return 0;
      }
      int available = toInt(row.get("available_copies"));
      if (available <= 0) {
        return 0;
      }
      row.put("available_copies", available - 1);
      return 1;
    }

    if (normalized.startsWith("update books set available_copies = available_copies + 1")) {
      Long id = toLong(params.get("id"));
      Map<String, Object> row = books.get(id);
      if (row == null) {
        return 0;
      }
      int available = toInt(row.get("available_copies"));
      int total = toInt(row.get("total_copies"));
      if (available >= total) {
        return 0;
      }
      row.put("available_copies", available + 1);
      return 1;
    }

    if (normalized.startsWith("insert into members")) {
      long id = memberSeq++;
      Map<String, Object> row = new HashMap<>();
      row.put("id", id);
      row.put("name", params.get("name"));
      row.put("email", params.get("email"));
      row.put("role_id", params.get("roleId"));
      members.put(id, row);
      return 1;
    }

    if (normalized.startsWith("update members set")) {
      Long id = toLong(params.get("id"));
      Map<String, Object> row = members.get(id);
      if (row == null) {
        return 0;
      }
      row.put("name", params.get("name"));
      row.put("email", params.get("email"));
      row.put("role_id", params.get("roleId"));
      return 1;
    }

    if (normalized.startsWith("delete from members")) {
      Long id = toLong(params.get("id"));
      return members.remove(id) == null ? 0 : 1;
    }

    if (normalized.startsWith("insert into loans")) {
      long id = loanSeq++;
      Map<String, Object> row = new HashMap<>();
      row.put("id", id);
      row.put("book_id", params.get("bookId"));
      row.put("member_id", params.get("memberId"));
      row.put("borrowed_at", params.get("borrowedAt"));
      row.put("due_date", params.get("dueDate"));
      row.put("returned_at", null);
      loans.put(id, row);
      return 1;
    }

    if (normalized.startsWith("update loans set returned_at")) {
      Long id = toLong(params.get("id"));
      Map<String, Object> row = loans.get(id);
      if (row == null || row.get("returned_at") != null) {
        return 0;
      }
      row.put("returned_at", params.get("returnedAt"));
      return 1;
    }

    return 0;
  }

  @Override
  public List<Map<String, Object>> query(String sql, Map<String, Object> params) {
    String normalized = normalize(sql);

    if (normalized.startsWith("select id, title, author, isbn")) {
      if (normalized.contains("where id")) {
        Long id = toLong(params.get("id"));
        return rowsOrEmpty(books.get(id));
      }
      if (normalized.contains("where isbn")) {
        String isbn = String.valueOf(params.get("isbn"));
        return findBy(books, "isbn", isbn);
      }
      return new ArrayList<>(books.values());
    }

    if (normalized.startsWith("select id from books")) {
      String isbn = String.valueOf(params.get("isbn"));
      List<Map<String, Object>> rows = findBy(books, "isbn", isbn);
      return rows.stream().map(this::onlyId).toList();
    }

    if (normalized.startsWith("select id, name, email")) {
      if (normalized.contains("where id")) {
        Long id = toLong(params.get("id"));
        return rowsOrEmpty(members.get(id));
      }
      if (normalized.contains("where email")) {
        String email = String.valueOf(params.get("email"));
        return findBy(members, "email", email);
      }
      return new ArrayList<>(members.values());
    }

    if (normalized.startsWith("select id from roles")) {
      if (normalized.contains("where name")) {
        String name = String.valueOf(params.get("name"));
        return findBy(roles, "name", name).stream().map(this::onlyId).toList();
      }
      if (normalized.contains("where id")) {
        Long id = toLong(params.get("id"));
        return rowsOrEmpty(roles.get(id)).stream().map(this::onlyId).toList();
      }
    }

    if (normalized.startsWith("select id from members")) {
      String email = String.valueOf(params.get("email"));
      List<Map<String, Object>> rows = findBy(members, "email", email);
      return rows.stream().map(this::onlyId).toList();
    }

    if (normalized.startsWith("select id, book_id, member_id")) {
      if (normalized.contains("where id")) {
        Long id = toLong(params.get("id"));
        return rowsOrEmpty(loans.get(id));
      }
      if (normalized.contains("where book_id")) {
        Long bookId = toLong(params.get("bookId"));
        Long memberId = toLong(params.get("memberId"));
        Instant borrowedAt = (Instant) params.get("borrowedAt");
        return findLoanByKeys(bookId, memberId, borrowedAt);
      }
      return new ArrayList<>(loans.values());
    }

    if (normalized.startsWith("select count(*) as count from loans")) {
      Long memberId = toLong(params.get("memberId"));
      long count = loans.values().stream()
          .filter(row -> memberId.equals(toLong(row.get("member_id"))))
          .filter(row -> row.get("returned_at") == null)
          .filter(row -> !normalized.contains("due_date")
              || ((Instant) row.get("due_date")).isBefore((Instant) params.get("now")))
          .count();
      Map<String, Object> result = new HashMap<>();
      result.put("count", count);
      return List.of(result);
    }

    return List.of();
  }

  private List<Map<String, Object>> rowsOrEmpty(Map<String, Object> row) {
    if (row == null) {
      return List.of();
    }
    return List.of(new HashMap<>(row));
  }

  private List<Map<String, Object>> findBy(Map<Long, Map<String, Object>> source, String key, String value) {
    List<Map<String, Object>> results = new ArrayList<>();
    for (Map<String, Object> row : source.values()) {
      Object rowValue = row.get(key);
      if (rowValue != null && rowValue.toString().equals(value)) {
        results.add(new HashMap<>(row));
      }
    }
    return results;
  }

  private List<Map<String, Object>> findLoanByKeys(Long bookId, Long memberId, Instant borrowedAt) {
    List<Map<String, Object>> results = new ArrayList<>();
    for (Map<String, Object> row : loans.values()) {
      if (bookId.equals(toLong(row.get("book_id")))
          && memberId.equals(toLong(row.get("member_id")))
          && borrowedAt.equals(row.get("borrowed_at"))) {
        results.add(new HashMap<>(row));
      }
    }
    return results;
  }

  private Map<String, Object> onlyId(Map<String, Object> row) {
    Map<String, Object> result = new HashMap<>();
    result.put("id", row.get("id"));
    return result;
  }

  private void insertRole(String name) {
    long id = roleSeq++;
    Map<String, Object> row = new HashMap<>();
    row.put("id", id);
    row.put("name", name);
    roles.put(id, row);
  }

  private String normalize(String sql) {
    return sql == null ? "" : sql.trim().toLowerCase();
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

  private int toInt(Object value) {
    if (value == null) {
      return 0;
    }
    if (value instanceof Number number) {
      return number.intValue();
    }
    return Integer.parseInt(value.toString());
  }
}
