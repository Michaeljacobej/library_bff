package com.example.library.domain;

import java.time.Instant;

public class Loan {
  private Long id;

  private Book book;

  private Member member;

  private Instant borrowedAt;

  private Instant dueDate;

  private Instant returnedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Book getBook() {
    return book;
  }

  public void setBook(Book book) {
    this.book = book;
  }

  public Member getMember() {
    return member;
  }

  public void setMember(Member member) {
    this.member = member;
  }

  public Instant getBorrowedAt() {
    return borrowedAt;
  }

  public void setBorrowedAt(Instant borrowedAt) {
    this.borrowedAt = borrowedAt;
  }

  public Instant getDueDate() {
    return dueDate;
  }

  public void setDueDate(Instant dueDate) {
    this.dueDate = dueDate;
  }

  public Instant getReturnedAt() {
    return returnedAt;
  }

  public void setReturnedAt(Instant returnedAt) {
    this.returnedAt = returnedAt;
  }
}
