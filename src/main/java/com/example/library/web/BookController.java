package com.example.library.web;

import com.example.library.domain.Book;
import com.example.library.service.BookService;
import com.example.library.web.dto.BookRequest;
import com.example.library.web.dto.BookResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {
  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','MEMBER')")
  public List<BookResponse> list() {
    return bookService.list().stream().map(BookController::toResponse).toList();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','MEMBER')")
  public BookResponse get(@PathVariable Long id) {
    return toResponse(bookService.get(id));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
  public BookResponse create(@Valid @RequestBody BookRequest request) {
    Book book = new Book();
    book.setTitle(request.title());
    book.setAuthor(request.author());
    book.setIsbn(request.isbn());
    book.setTotalCopies(request.totalCopies());
    book.setAvailableCopies(request.availableCopies());
    return toResponse(bookService.create(book));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
  public BookResponse update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
    Book book = new Book();
    book.setTitle(request.title());
    book.setAuthor(request.author());
    book.setIsbn(request.isbn());
    book.setTotalCopies(request.totalCopies());
    book.setAvailableCopies(request.availableCopies());
    return toResponse(bookService.update(id, book));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
  public void delete(@PathVariable Long id) {
    bookService.delete(id);
  }

  private static BookResponse toResponse(Book book) {
    return new BookResponse(
        book.getId(),
        book.getTitle(),
        book.getAuthor(),
        book.getIsbn(),
        book.getTotalCopies(),
        book.getAvailableCopies()
    );
  }
}
