package com.example.library.web;

import com.example.library.domain.Member;
import com.example.library.service.MemberService;
import com.example.library.web.dto.MemberRequest;
import com.example.library.web.dto.MemberResponse;
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
@RequestMapping("/api/members")
public class MemberController {
  private final MemberService memberService;

  public MemberController(MemberService memberService) {
    this.memberService = memberService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
  public List<MemberResponse> list() {
    return memberService.list().stream().map(MemberController::toResponse).toList();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
  public MemberResponse get(@PathVariable Long id) {
    return toResponse(memberService.get(id));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
  public MemberResponse create(@Valid @RequestBody MemberRequest request) {
    Member member = new Member();
    member.setName(request.name());
    member.setEmail(request.email());
    return toResponse(memberService.create(member));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
  public MemberResponse update(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
    Member member = new Member();
    member.setName(request.name());
    member.setEmail(request.email());
    return toResponse(memberService.update(id, member));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
  public void delete(@PathVariable Long id) {
    memberService.delete(id);
  }

  private static MemberResponse toResponse(Member member) {
    return new MemberResponse(member.getId(), member.getName(), member.getEmail());
  }
}
