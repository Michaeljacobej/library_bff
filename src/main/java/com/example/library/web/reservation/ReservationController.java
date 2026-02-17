package com.example.library.web.reservation;

import com.example.library.service.ReservationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
  private final ReservationService reservationService;

  public ReservationController(ReservationService reservationService) {
    this.reservationService = reservationService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
  public List<ReservationResponse> list() {
    return reservationService.list().stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
  public ReservationResponse get(@PathVariable Long id) {
    return toResponse(reservationService.get(id));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','MEMBER')")
  public ReservationResponse create(@Valid @RequestBody ReservationRequest request) {
    String roleName = reservationService.resolveRoleNameForMember(request.memberId());
    Map<String, Object> row = reservationService.create(request.bookId(), request.memberId(), roleName);
    return toResponse(row);
  }

  @PostMapping("/{id}/cancel")
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','MEMBER')")
  public void cancel(@PathVariable Long id) {
    reservationService.cancel(id);
  }

  private ReservationResponse toResponse(Map<String, Object> row) {
    return new ReservationResponse(
        reservationService.toLong(row, "id"),
        reservationService.toLong(row, "book_id"),
        reservationService.toLong(row, "member_id"),
        reservationService.toString(row, "role_name"),
        reservationService.toString(row, "status"),
        reservationService.toInstant(row, "created_at"),
        reservationService.toInstant(row, "fulfilled_at"),
        reservationService.toInstant(row, "canceled_at")
    );
  }
}
