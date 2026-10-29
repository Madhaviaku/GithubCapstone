package com.library.catalog;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ApiDocumentationGenerator {

    public static void main(String[] args) throws Exception {
        String content = generateMarkdown();
        Path docsPath = Path.of("docs", "api.md");
        Files.createDirectories(docsPath.getParent());
        Files.writeString(docsPath, content, StandardCharsets.UTF_8);
        System.out.println("Generated API documentation at " + docsPath.toAbsolutePath());
    }

    public static String generateMarkdown() {
        return "# Library Book Management API\n\n"
                + "## Base URL\n\n"
                + "`http://localhost:8080/api`\n\n"
                + "## Overview\n\n"
                + "This document describes the book catalog endpoints required by the Library Book Management Catalog project.\n\n"
                + "## Endpoints\n\n"
                + "### GET /api/books\n\n"
                + "- Description: Retrieve all books with pagination support\n"
                + "- Query parameters:\n"
                + "  - `page` (optional, default `0`)\n"
                + "  - `size` (optional, default `20`)\n"
                + "- Response: `200 OK` with paginated list of books\n"
                + "- Errors: `500` for server failures\n\n"
                + "### GET /api/books/{isbn}\n\n"
                + "- Description: Retrieve a single book by ISBN\n"
                + "- Path parameter: `isbn`\n"
                + "- Response: `200 OK` with a book payload\n"
                + "- Errors: `400` for invalid ISBN, `404` when the book does not exist\n\n"
                + "### POST /api/books\n\n"
                + "- Description: Create a new book\n"
                + "- Request body: ISBN, title, author, publisher, publicationDate, genre, quantityAvailable\n"
                + "- Response: `201 Created` with the created book record\n"
                + "- Errors: `400` for validation issues, `409` for duplicate ISBN\n\n"
                + "### PUT /api/books/{isbn}\n\n"
                + "- Description: Update an existing book by ISBN\n"
                + "- Path parameter: `isbn`\n"
                + "- Request body: partial or full update payload\n"
                + "- Response: `200 OK` with the updated book\n"
                + "- Errors: `400` for invalid update payload, `404` when the book does not exist\n\n"
                + "### DELETE /api/books/{isbn}\n\n"
                + "- Description: Delete a book by ISBN\n"
                + "- Path parameter: `isbn`\n"
                + "- Response: `204 No Content`\n"
                + "- Errors: `404` when the book does not exist\n\n"
                + "### GET /api/books/search\n\n"
                + "- Description: Search books by partial or full title match\n"
                + "- Query parameters:\n"
                + "  - `title` (required)\n"
                + "  - `page` (optional, default `0`)\n"
                + "  - `size` (optional, default `20`)\n"
                + "- Response: `200 OK` with paginated matching books\n\n"
                + "## Validation Rules\n\n"
                + "- ISBN must be 10 or 13 digits with optional hyphens\n"
                + "- Title, author, and genre are required\n"
                + "- Publisher optional, max length 100\n"
                + "- Publication date optional and cannot be in the future\n"
                + "- Quantity must be non-negative\n\n"
                + "## Error Payload Format\n\n"
                + "`code`, `message`, `timestamp`, and optional `errors` for validation failures.\n";
    }
}
