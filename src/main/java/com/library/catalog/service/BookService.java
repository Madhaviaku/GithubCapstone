package com.library.catalog.service;

import com.library.catalog.dto.BookCreateRequest;
import com.library.catalog.dto.BookResponse;
import com.library.catalog.dto.BookUpdateRequest;
import com.library.catalog.entity.Book;
import com.library.catalog.exception.BookNotFoundException;
import com.library.catalog.exception.DuplicateIsbnException;
import com.library.catalog.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public Page<BookResponse> getBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAll(pageable).map(this::toResponse);
    }

    public BookResponse getBookByIsbn(String isbn) {
        String normalizedIsbn = normalizeIsbn(isbn);
        Book book = repository.findByIsbn(normalizedIsbn)
                .orElseThrow(() -> new BookNotFoundException(normalizedIsbn));
        return toResponse(book);
    }

    @Transactional
    public BookResponse createBook(BookCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        String isbn = normalizeIsbn(request.isbn());
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.author() == null || request.author().isBlank()) {
            throw new IllegalArgumentException("Author is required");
        }
        if (request.genre() == null || request.genre().isBlank()) {
            throw new IllegalArgumentException("Genre is required");
        }
        if (request.quantityAvailable() != null && request.quantityAvailable() < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }
        if (repository.existsByIsbn(isbn)) {
            throw new DuplicateIsbnException(isbn);
        }

        Book book = new Book(
                isbn,
                request.title().trim(),
                request.author().trim(),
                request.publisher() == null ? null : request.publisher().trim(),
                request.publicationDate(),
                request.genre().trim(),
                request.quantityAvailable() == null ? 0 : request.quantityAvailable()
        );
        return toResponse(repository.save(book));
    }

    @Transactional
    public BookResponse updateBook(String isbn, BookUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        String normalizedIsbn = normalizeIsbn(isbn);
        Book existing = repository.findByIsbn(normalizedIsbn)
                .orElseThrow(() -> new BookNotFoundException(normalizedIsbn));

        if (request.title() != null && request.title().isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        if (request.author() != null && request.author().isBlank()) {
            throw new IllegalArgumentException("Author cannot be blank");
        }
        if (request.genre() != null && request.genre().isBlank()) {
            throw new IllegalArgumentException("Genre cannot be blank");
        }
        if (request.quantityAvailable() != null && request.quantityAvailable() < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }

        if (request.title() != null) {
            existing.setTitle(request.title().trim());
        }
        if (request.author() != null) {
            existing.setAuthor(request.author().trim());
        }
        if (request.publisher() != null) {
            existing.setPublisher(request.publisher().trim());
        }
        if (request.publicationDate() != null) {
            existing.setPublicationDate(request.publicationDate());
        }
        if (request.genre() != null) {
            existing.setGenre(request.genre().trim());
        }
        if (request.quantityAvailable() != null) {
            existing.setQuantityAvailable(request.quantityAvailable());
        }
        if (request.isbn() != null && !request.isbn().isBlank()) {
            String replacementIsbn = normalizeIsbn(request.isbn());
            if (!existing.getIsbn().equals(replacementIsbn) && repository.existsByIsbn(replacementIsbn)) {
                throw new DuplicateIsbnException(replacementIsbn);
            }
            existing.setIsbn(replacementIsbn);
        }

        return toResponse(repository.save(existing));
    }

    @Transactional
    public void deleteBook(String isbn) {
        String normalizedIsbn = normalizeIsbn(isbn);
        Book existing = repository.findByIsbn(normalizedIsbn)
                .orElseThrow(() -> new BookNotFoundException(normalizedIsbn));
        repository.delete(existing);
    }

    public Page<BookResponse> searchBooks(String title, int page, int size) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Search title is required");
        }
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByTitleContainingIgnoreCase(title.trim(), pageable).map(this::toResponse);
    }

    public String normalizeIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN is required");
        }
        String normalized = isbn.replace("-", "").trim();
        if (!normalized.matches("\\d{10}|\\d{13}")) {
            throw new IllegalArgumentException("Invalid ISBN format");
        }
        return normalized;
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getPublicationDate(),
                book.getGenre(),
                book.getQuantityAvailable(),
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }
}
