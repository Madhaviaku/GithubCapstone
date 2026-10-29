package com.library.catalog;

import com.library.catalog.dto.BookCreateRequest;
import com.library.catalog.dto.BookUpdateRequest;
import com.library.catalog.entity.Book;
import com.library.catalog.exception.BookNotFoundException;
import com.library.catalog.exception.DuplicateIsbnException;
import com.library.catalog.repository.BookRepository;
import com.library.catalog.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void createBookShouldPersistAndNormalizeIsbn() {
        BookCreateRequest request = new BookCreateRequest(
                "978-3-16-148410-0",
                "Effective Java",
                "Joshua Bloch",
                "Addison-Wesley",
                LocalDate.of(2018, 1, 1),
                "Programming",
                5
        );

        when(bookRepository.existsByIsbn("9783161484100")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        var created = bookService.createBook(request);

        assertEquals("9783161484100", created.isbn());
        assertEquals("Effective Java", created.title());
    }

    @Test
    void createBookShouldFailForDuplicateIsbn() {
        BookCreateRequest request = new BookCreateRequest(
                "9783161484100",
                "Effective Java",
                "Joshua Bloch",
                "Addison-Wesley",
                LocalDate.of(2018, 1, 1),
                "Programming",
                5
        );

        when(bookRepository.existsByIsbn("9783161484100")).thenReturn(true);

        assertThrows(DuplicateIsbnException.class, () -> bookService.createBook(request));
    }

    @Test
    void getBookShouldFailWhenMissing() {
        when(bookRepository.findByIsbn("9783161484100")).thenReturn(java.util.Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.getBookByIsbn("9783161484100"));
    }

    @Test
    void updateBookShouldRejectInvalidQuantity() {
        Book existing = new Book("9783161484100", "Effective Java", "Joshua Bloch", "Addison-Wesley",
                LocalDate.of(2018, 1, 1), "Programming", 5);
        existing.setId(1L);

        when(bookRepository.findByIsbn("9783161484100")).thenReturn(java.util.Optional.of(existing));

        BookUpdateRequest request = new BookUpdateRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                -1
        );

        assertThrows(IllegalArgumentException.class, () -> bookService.updateBook("9783161484100", request));
    }
}
