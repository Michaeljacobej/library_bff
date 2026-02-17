package com.example.library.web.dto;

public record BookResponse(
    Long id,
    String title,
    String author,
    String isbn,
    int totalCopies,
    int availableCopies
) {}
