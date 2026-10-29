package com.library.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record BookCreateRequest(
        @NotBlank(message = "ISBN is required")
        @Pattern(regexp = "^(?:\\d{10}|\\d{13}|\\d[\\d-]{8,}\\d)$", message = "Invalid ISBN format")
        String isbn,

        @NotBlank(message = "Title is required")
        @Size(min = 1, max = 200, message = "Title length must be between 1 and 200 characters")
        String title,

        @NotBlank(message = "Author is required")
        @Size(min = 1, max = 100, message = "Author length must be between 1 and 100 characters")
        String author,

        @Size(max = 100, message = "Publisher length must not exceed 100 characters")
        String publisher,

        @PastOrPresent(message = "Publication date cannot be in the future")
        LocalDate publicationDate,

        @NotBlank(message = "Genre is required")
        @Size(min = 1, max = 50, message = "Genre length must be between 1 and 50 characters")
        String genre,

        @NotNull(message = "Quantity available is required")
        @Min(value = 0, message = "Quantity must be non-negative")
        Integer quantityAvailable
) {
}
