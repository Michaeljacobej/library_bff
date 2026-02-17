package com.example.library.web;

import com.example.library.domain.Loan;
import com.example.library.service.LoanService;
import com.example.library.web.dto.LoanRequest;
import com.example.library.web.dto.LoanResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
  private final LoanService loanService;

  public LoanController(LoanService loanService) {
    this.loanService = loanService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
  public List<LoanResponse> list() {
    return loanService.list().stream().map(LoanController::toResponse).toList();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
  public LoanResponse get(@PathVariable Long id) {
    return toResponse(loanService.get(id));
  }

  @PostMapping("/borrow")
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','MEMBER')")
  public LoanResponse borrow(@Valid @RequestBody LoanRequest request) {
    return toResponse(loanService.borrow(request.bookId(), request.memberId()));
  }

  @GetMapping("/search")
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
  public List<LoanResponse> search(@RequestParam(required = false) Long memberId,
                                   @RequestParam(required = false) Long bookId,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) Instant from,
                                   @RequestParam(required = false) Instant to) {
    return loanService.search(memberId, bookId, status, from, to)
        .stream()
        .map(LoanController::toResponse)
        .toList();
  }

  @PostMapping("/{id}/return")
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','MEMBER')")
  public LoanResponse returnLoan(@PathVariable Long id) {
    return toResponse(loanService.returnLoan(id));
  }

  private static LoanResponse toResponse(Loan loan) {
    return new LoanResponse(
        loan.getId(),
        loan.getBook().getId(),
        loan.getMember().getId(),
        loan.getBorrowedAt(),
        loan.getDueDate(),
        loan.getReturnedAt()
    );
  }
}
