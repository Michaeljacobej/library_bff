package com.example.library.service;

import com.example.library.config.BorrowingProperties;
import com.example.library.domain.Book;
import com.example.library.domain.Loan;
import com.example.library.domain.Member;
import com.example.library.sqladapter.SqlAdapterClient;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class LoanService {
  private final BorrowingProperties borrowingProperties;
  private final SqlAdapterClient sqlAdapterClient;

  public LoanService(BorrowingProperties borrowingProperties,
                     SqlAdapterClient sqlAdapterClient) {
    this.borrowingProperties = borrowingProperties;
    this.sqlAdapterClient = sqlAdapterClient;
  }

  public List<Loan> list() {
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select id, book_id, member_id, borrowed_at, due_date, returned_at from loans order by id",
        Map.of()
    );
    return rows.stream().map(LoanService::mapLoan).toList();
  }

  public Loan get(Long id) {
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select id, book_id, member_id, borrowed_at, due_date, returned_at from loans where id = :id",
        Map.of("id", id)
    );
    if (rows.isEmpty()) {
      throw new NotFoundException("Loan not found");
    }
    return mapLoan(rows.get(0));
  }

  public Loan borrow(Long bookId, Long memberId) {
    Book book = getBook(bookId);
    Member member = getMember(memberId);

    if (book.getAvailableCopies() <= 0) {
      throw new BusinessRuleException("No available copies to borrow");
    }

    long activeLoans = countActiveLoans(memberId);
    if (activeLoans >= borrowingProperties.getMaxActiveLoansPerMember()) {
      throw new BusinessRuleException("Member has reached maximum active loans");
    }

    boolean hasOverdue = hasOverdueLoan(memberId, Instant.now());
    if (hasOverdue) {
      throw new BusinessRuleException("Member has overdue loans");
    }

    Instant borrowedAt = Instant.now();
    Instant dueDate = borrowedAt.plus(borrowingProperties.getMaxLoanDays(), ChronoUnit.DAYS);
    Map<String, Object> bookParams = new HashMap<>();
    bookParams.put("id", book.getId());
    int bookRows = sqlAdapterClient.execute(
        "update books set available_copies = available_copies - 1 "
            + "where id = :id and available_copies > 0",
        bookParams
    );
    if (bookRows <= 0) {
      throw new BusinessRuleException("No available copies to borrow");
    }

    Map<String, Object> loanParams = new HashMap<>();
    loanParams.put("bookId", book.getId());
    loanParams.put("memberId", member.getId());
    loanParams.put("borrowedAt", borrowedAt);
    loanParams.put("dueDate", dueDate);

    int loanRows = sqlAdapterClient.execute(
        "insert into loans (book_id, member_id, borrowed_at, due_date, returned_at) "
            + "values (:bookId, :memberId, :borrowedAt, :dueDate, null)",
        loanParams
    );
    if (loanRows <= 0) {
      throw new BusinessRuleException("Loan insert failed");
    }

    return getLoanByKeys(book.getId(), member.getId(), borrowedAt);
  }

  public Loan returnLoan(Long loanId) {
    Loan loan = get(loanId);
    if (loan.getReturnedAt() != null) {
      throw new BusinessRuleException("Loan already returned");
    }
    Instant returnedAt = Instant.now();

    Map<String, Object> loanParams = new HashMap<>();
    loanParams.put("id", loanId);
    loanParams.put("returnedAt", returnedAt);
    int loanRows = sqlAdapterClient.execute(
        "update loans set returned_at = :returnedAt where id = :id and returned_at is null",
        loanParams
    );
    if (loanRows <= 0) {
      throw new BusinessRuleException("Loan already returned");
    }

    Map<String, Object> bookParams = new HashMap<>();
    bookParams.put("id", loan.getBook().getId());
    sqlAdapterClient.execute(
        "update books set available_copies = available_copies + 1 "
            + "where id = :id and available_copies < total_copies",
        bookParams
    );

    return get(loanId);
  }

  private Book getBook(Long id) {
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select id, title, author, isbn, total_copies, available_copies from books where id = :id",
        Map.of("id", id)
    );
    if (rows.isEmpty()) {
      throw new NotFoundException("Book not found");
    }
    Map<String, Object> row = rows.get(0);
    Book book = new Book();
    book.setId(toLong(row, "id"));
    book.setTitle(toString(row, "title"));
    book.setAuthor(toString(row, "author"));
    book.setIsbn(toString(row, "isbn"));
    book.setTotalCopies(toInt(row, "total_copies"));
    book.setAvailableCopies(toInt(row, "available_copies"));
    return book;
  }

  private Member getMember(Long id) {
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select id, name, email, role_id from members where id = :id",
        Map.of("id", id)
    );
    if (rows.isEmpty()) {
      throw new NotFoundException("Member not found");
    }
    Map<String, Object> row = rows.get(0);
    Member member = new Member();
    member.setId(toLong(row, "id"));
    member.setName(toString(row, "name"));
    member.setEmail(toString(row, "email"));
    member.setRoleId(toLong(row, "role_id"));
    return member;
  }

  private long countActiveLoans(Long memberId) {
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select count(*) as count from loans where member_id = :memberId and returned_at is null",
        Map.of("memberId", memberId)
    );
    if (rows.isEmpty()) {
      return 0L;
    }
    return toLong(rows.get(0), "count");
  }

  private boolean hasOverdueLoan(Long memberId, Instant now) {
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select count(*) as count from loans "
            + "where member_id = :memberId and returned_at is null and due_date < :now",
        Map.of("memberId", memberId, "now", now)
    );
    if (rows.isEmpty()) {
      return false;
    }
    return toLong(rows.get(0), "count") > 0;
  }

  private Loan getLoanByKeys(Long bookId, Long memberId, Instant borrowedAt) {
    Map<String, Object> params = new HashMap<>();
    params.put("bookId", bookId);
    params.put("memberId", memberId);
    params.put("borrowedAt", borrowedAt);
    List<Map<String, Object>> rows = sqlAdapterClient.query(
        "select id, book_id, member_id, borrowed_at, due_date, returned_at from loans "
            + "where book_id = :bookId and member_id = :memberId and borrowed_at = :borrowedAt",
        params
    );
    if (rows.isEmpty()) {
      throw new NotFoundException("Loan not found after insert");
    }
    return mapLoan(rows.get(0));
  }

  private static Loan mapLoan(Map<String, Object> row) {
    Loan loan = new Loan();
    loan.setId(toLong(row, "id"));

    Book book = new Book();
    book.setId(toLong(row, "book_id"));
    loan.setBook(book);

    Member member = new Member();
    member.setId(toLong(row, "member_id"));
    loan.setMember(member);

    loan.setBorrowedAt(toInstant(row, "borrowed_at"));
    loan.setDueDate(toInstant(row, "due_date"));
    loan.setReturnedAt(toInstant(row, "returned_at"));
    return loan;
  }

  private static Instant toInstant(Map<String, Object> row, String key) {
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
