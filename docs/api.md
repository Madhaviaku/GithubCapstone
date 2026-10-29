# Library Book Management API

## Base URL

`http://localhost:8080/api`

## Overview

This document describes the book catalog endpoints required by the Library Book Management Catalog project.

## Endpoints

### GET /api/books

- Description: Retrieve all books with pagination support
- Query parameters:
  - `page` (optional, default `0`)
  - `size` (optional, default `20`)
- Response: `200 OK` with paginated list of books
- Errors: `500` for server failures

### GET /api/books/{isbn}

- Description: Retrieve a single book by ISBN
- Path parameter: `isbn`
- Response: `200 OK` with a book payload
- Errors: `400` for invalid ISBN, `404` when the book does not exist

### POST /api/books

- Description: Create a new book
- Request body: ISBN, title, author, publisher, publicationDate, genre, quantityAvailable
- Response: `201 Created` with the created book record
- Errors: `400` for validation issues, `409` for duplicate ISBN

### PUT /api/books/{isbn}

- Description: Update an existing book by ISBN
- Path parameter: `isbn`
- Request body: partial or full update payload
- Response: `200 OK` with the updated book
- Errors: `400` for invalid update payload, `404` when the book does not exist

### DELETE /api/books/{isbn}

- Description: Delete a book by ISBN
- Path parameter: `isbn`
- Response: `204 No Content`
- Errors: `404` when the book does not exist

### GET /api/books/search

- Description: Search books by partial or full title match
- Query parameters:
  - `title` (required)
  - `page` (optional, default `0`)
  - `size` (optional, default `20`)
- Response: `200 OK` with paginated matching books

## Validation Rules

- ISBN must be 10 or 13 digits with optional hyphens
- Title, author, and genre are required
- Publisher optional, max length 100
- Publication date optional and cannot be in the future
- Quantity must be non-negative

## Error Payload Format

`code`, `message`, `timestamp`, and optional `errors` for validation failures.
