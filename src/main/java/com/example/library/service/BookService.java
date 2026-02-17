package com.example.library.service;

import com.example.library.domain.Book;
import com.example.library.sqladapter.SqlAdapterClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class BookService {
  private final SqlAdapterClient sqlAdapterClient;

  public BookService(SqlAdapterClient sqlAdapterClient) {
    this.sqlAdapterClient = sqlAdapterClient;
  }

  public List<Book> list() {
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select id, title, author, isbn, total_copies, available_copies from books order by id",
        Map.of()
    );
    return rows.stream().map(BookService::mapBook).toList();
  }

  public Book get(Long id) {
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select id, title, author, isbn, total_copies, available_copies from books where id = :id",
        Map.of("id", id)
    );
    if (rows.isEmpty()) {
      throw new NotFoundException("Book not found");
    }
    return mapBook(rows.get(0));
  }

  public Book create(Book book) {
    if (book.getAvailableCopies() > book.getTotalCopies()) {
      throw new BusinessRuleException("Available copies cannot exceed total copies");
    }
    if (existsByIsbn(book.getIsbn())) {
      throw new BusinessRuleException("ISBN already exists");
    }
    Map<String, Object> params = new HashMap<>();
    params.put("title", book.getTitle());
    params.put("author", book.getAuthor());
    params.put("isbn", book.getIsbn());
    params.put("totalCopies", book.getTotalCopies());
    params.put("availableCopies", book.getAvailableCopies());

    int rows = sqlAdapterClient.execute(
        "insert into books (title, author, isbn, total_copies, available_copies) "
            + "values (:title, :author, :isbn, :totalCopies, :availableCopies)",
        params
    );
    if (rows <= 0) {
      throw new BusinessRuleException("Book insert failed");
    }

    return getByIsbn(book.getIsbn());
  }

  public Book update(Long id, Book update) {
    if (update.getAvailableCopies() > update.getTotalCopies()) {
      throw new BusinessRuleException("Available copies cannot exceed total copies");
    }
    Book existing = get(id);
    if (!existing.getIsbn().equals(update.getIsbn()) && existsByIsbn(update.getIsbn())) {
      throw new BusinessRuleException("ISBN already exists");
    }
    Map<String, Object> params = new HashMap<>();
    params.put("id", id);
    params.put("title", update.getTitle());
    params.put("author", update.getAuthor());
    params.put("isbn", update.getIsbn());
    params.put("totalCopies", update.getTotalCopies());
    params.put("availableCopies", update.getAvailableCopies());

    int rows = sqlAdapterClient.execute(
        "update books set title = :title, author = :author, isbn = :isbn, "
            + "total_copies = :totalCopies, available_copies = :availableCopies where id = :id",
        params
    );
    if (rows <= 0) {
      throw new NotFoundException("Book not found");
    }

    return get(id);
  }

  public void delete(Long id) {
    Map<String, Object> params = new HashMap<>();
    params.put("id", id);
    int rows = sqlAdapterClient.execute("delete from books where id = :id", params);
    if (rows <= 0) {
      throw new NotFoundException("Book not found");
    }
  }

  private boolean existsByIsbn(String isbn) {
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select id from books where isbn = :isbn",
        Map.of("isbn", isbn)
    );
    return !rows.isEmpty();
  }

  private Book getByIsbn(String isbn) {
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select id, title, author, isbn, total_copies, available_copies from books where isbn = :isbn",
        Map.of("isbn", isbn)
    );
    if (rows.isEmpty()) {
      throw new NotFoundException("Book not found after insert");
    }
    return mapBook(rows.get(0));
  }

  private static Book mapBook(Map<String, Object> row) {
    Book book = new Book();
    book.setId(toLong(row, "id"));
    book.setTitle(toString(row, "title"));
    book.setAuthor(toString(row, "author"));
    book.setIsbn(toString(row, "isbn"));
    book.setTotalCopies(toInt(row, "total_copies"));
    book.setAvailableCopies(toInt(row, "available_copies"));
    return book;
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

  private static int toInt(Map<String, Object> row, String key) {
    Object value = getValue(row, key);
    if (value == null) {
      return 0;
    }
    if (value instanceof Number number) {
      return number.intValue();
    }
    return Integer.parseInt(value.toString());
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
