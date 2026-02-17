package com.example.library;

import com.example.library.config.BorrowingProperties;
import com.example.library.domain.Book;
import com.example.library.domain.Loan;
import com.example.library.domain.Member;
import com.example.library.service.BookService;
import com.example.library.service.BusinessRuleException;
import com.example.library.service.LoanService;
import com.example.library.service.MemberService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class LoanServiceTest {
  private final InMemorySqlAdapterClient adapter = new InMemorySqlAdapterClient();
  private final BorrowingProperties properties = new BorrowingProperties();
  private final BookService bookService = new BookService(adapter);
  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  private final MemberService memberService = new MemberService(adapter, passwordEncoder);
  private final LoanService loanService = new LoanService(properties, adapter);

  @Test
  void borrowCreatesLoanAndDecrementsCopies() {
    properties.setMaxActiveLoansPerMember(5);
    properties.setMaxLoanDays(14);

    Book book = new Book();
    book.setTitle("Clean Code");
    book.setAuthor("Robert C. Martin");
    book.setIsbn("isbn-1");
    book.setTotalCopies(2);
    book.setAvailableCopies(2);
    Book createdBook = bookService.create(book);

    Member member = new Member();
    member.setName("Alice");
    member.setEmail("alice@example.com");
    member.setRoleId(3L);
    member.setPassword("member-pass");
    Member createdMember = memberService.create(member);

    Loan loan = loanService.borrow(createdBook.getId(), createdMember.getId());

    Book updated = bookService.get(createdBook.getId());
    Assertions.assertEquals(1, updated.getAvailableCopies());
    Assertions.assertNotNull(loan.getBorrowedAt());
    Assertions.assertNotNull(loan.getDueDate());
  }

  @Test
  void borrowFailsWhenMemberOverLimit() {
    properties.setMaxActiveLoansPerMember(1);
    properties.setMaxLoanDays(14);

    Book book = new Book();
    book.setTitle("Refactoring");
    book.setAuthor("Martin Fowler");
    book.setIsbn("isbn-2");
    book.setTotalCopies(10);
    book.setAvailableCopies(10);
    Book createdBook = bookService.create(book);

    Member member = new Member();
    member.setName("Bob");
    member.setEmail("bob@example.com");
    member.setRoleId(3L);
    member.setPassword("member-pass");
    Member createdMember = memberService.create(member);

    loanService.borrow(createdBook.getId(), createdMember.getId());

    Assertions.assertThrows(BusinessRuleException.class,
        () -> loanService.borrow(createdBook.getId(), createdMember.getId()));
  }

  @Test
  void borrowFailsWhenOverdueLoanExists() {
    properties.setMaxActiveLoansPerMember(5);
    properties.setMaxLoanDays(14);

    Book book = new Book();
    book.setTitle("Domain-Driven Design");
    book.setAuthor("Eric Evans");
    book.setIsbn("isbn-3");
    book.setTotalCopies(3);
    book.setAvailableCopies(3);
    Book createdBook = bookService.create(book);

    Member member = new Member();
    member.setName("Carol");
    member.setEmail("carol@example.com");
    member.setRoleId(3L);
    member.setPassword("member-pass");
    Member createdMember = memberService.create(member);

    Map<String, Object> params = new HashMap<>();
    params.put("bookId", createdBook.getId());
    params.put("memberId", createdMember.getId());
    params.put("borrowedAt", Instant.now().minus(30, ChronoUnit.DAYS));
    params.put("dueDate", Instant.now().minus(1, ChronoUnit.DAYS));
    adapter.execute(
        "insert into loans (book_id, member_id, borrowed_at, due_date, returned_at) "
            + "values (:bookId, :memberId, :borrowedAt, :dueDate, null)",
        params
    );

    Assertions.assertThrows(BusinessRuleException.class,
        () -> loanService.borrow(createdBook.getId(), createdMember.getId()));
  }
}
