package com.library.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.catalog.dto.BookCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBookShouldPersistAndReturnCreatedBook() throws Exception {
        BookCreateRequest request = new BookCreateRequest(
                "9783161484100",
                "Effective Java",
                "Joshua Bloch",
                "Addison-Wesley",
                LocalDate.of(2018, 1, 1),
                "Programming",
                5
        );

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Book created successfully"))
                .andExpect(jsonPath("$.data.isbn").value("9783161484100"));

        mockMvc.perform(get("/api/books/9783161484100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Effective Java"));
    }

    @Test
    void getBookShouldReturnNotFoundForMissingIsbn() throws Exception {
        mockMvc.perform(get("/api/books/0000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Book with ISBN 0000000000 not found"));
    }

    @Test
    void createBookShouldFailWhenRequiredFieldsMissing() throws Exception {
        String payload = "{\"isbn\":\"\",\"title\":\"\",\"author\":\"\",\"genre\":\"\",\"quantityAvailable\":-1}";

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
