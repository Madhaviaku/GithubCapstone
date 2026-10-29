# Requirements.md - Library Book Management Catalog with Auto-Doc Sync

**Project Name:** Library Book Management Catalog with Auto-Doc Sync  
**Date:** October 14, 2026  
**Status:** ✅ APPROVED  
**Version:** 1.0.0

---

## 📋 Executive Summary

The Library Book Management System is a web application that allows Library Administrators to manage a digital catalog of books. The system supports full CRUD operations (Create, Read, Update, Delete) for books, title-based search functionality, and implements an automatic API documentation synchronization mechanism that ensures /docs/api.md stays in sync with the codebase at all times.

---

## 🎯 User Story

**As a** Library Administrator  
**I want to** add, view, search, edit, and delete books in the library catalog  
**So that** the catalog is always up-to-date and searchable

**Additional Requirement:** Whenever developers modify the catalog APIs, models, or fields in the code, the API markdown documentation (/docs/api.md) must automatically update to match the code. The build or commit should fail if the documentation is not in sync.

---

## 📌 Functional Requirements

### FR1: Book Catalog Management (CRUD Operations)

#### FR1.1 Create Book
- **Description:** Library Administrator can add a new book to the catalog
- **Input:** ISBN, Title, Author, Publisher, Publication Date, Genre, Quantity Available
- **Process:** Validate all inputs, check ISBN uniqueness, create book record
- **Output:** Success message with created book details or error message
- **Constraints:** ISBN must be unique; Title, Author, Genre are required

#### FR1.2 View All Books
- **Description:** Retrieve all books from the catalog with pagination
- **Input:** Page number, Page size (optional)
- **Process:** Query database, apply pagination, return book list
- **Output:** Paginated list of BookDTOs or empty list
- **Constraints:** Default page size = 20 books per page

#### FR1.3 View Single Book
- **Description:** Retrieve a specific book by ISBN
- **Input:** ISBN (string)
- **Process:** Query database by ISBN, return book details
- **Output:** BookDTO or 404 Not Found
- **Constraints:** ISBN must be valid format

#### FR1.4 Search Books by Title
- **Description:** Search for books using partial or full title matching
- **Input:** Search title (string), Page number, Page size (optional)
- **Process:** Case-insensitive search in title field, return paginated results
- **Output:** Paginated list of matching books or empty list
- **Constraints:** Supports partial matches (e.g., "Java" matches "Java Programming")

#### FR1.5 Update Book
- **Description:** Library Administrator can modify book information
- **Input:** ISBN (identifier), Updated fields (partial or full)
- **Process:** Validate ISBN exists, update specified fields, validate new data
- **Output:** Updated BookDTO or error message
- **Constraints:** Cannot change ISBN; fields are individually optional but must be valid if provided

#### FR1.6 Delete Book
- **Description:** Remove a book from the catalog
- **Input:** ISBN (string)
- **Process:** Find book by ISBN, delete record
- **Output:** 204 No Content or 404 Not Found
- **Constraints:** Soft delete is optional; hard delete is acceptable

### FR2: Data Validation

#### FR2.1 ISBN Validation
- **Format:** 10 or 13 digits (with optional hyphens for formatting)
- **Examples:** "978-3-16-148410-0" or "9783161484100"
- **Rule:** Must be unique across all books

#### FR2.2 Required Fields Validation
- **Title:** Required, 1-200 characters, non-empty string
- **Author:** Required, 1-100 characters, non-empty string
- **Genre:** Required, 1-50 characters, non-empty string

#### FR2.3 Optional Fields Validation
- **Publisher:** Optional, max 100 characters if provided
- **Publication Date:** Optional, must be valid date (not in future)
- **Quantity Available:** Optional, default = 0, must be non-negative integer

### FR3: Error Handling

#### FR3.1 HTTP Error Responses
| Status Code | Scenario | Message Format |
|---|---|---|
| 400 | Invalid input/validation failed | `{code: 400, message: "Detailed validation error", timestamp: ...}` |
| 404 | Book not found | `{code: 404, message: "Book with ISBN XXX not found", timestamp: ...}` |
| 409 | Duplicate ISBN | `{code: 409, message: "ISBN already exists in catalog", timestamp: ...}` |
| 500 | Server error | `{code: 500, message: "Internal server error", timestamp: ...}` |

#### FR3.2 Validation Error Details
- Return specific field validation errors
- Example: `{code: 400, message: "Validation failed", errors: {isbn: "Invalid ISBN format", title: "Title is required"}}`

### FR4: Auto-Doc Sync Mechanism

#### FR4.1 Automatic Documentation Generation
- **Trigger:** On application startup and after code changes
- **Mechanism:** Scan @RestController and @RequestMapping annotations
- **Output Format:** Markdown file at `/docs/api.md`
- **Content:** All 5 REST endpoints with full specifications

#### FR4.2 Documentation Sync Validation
- **Build Step:** Maven plugin validates /docs/api.md matches current code
- **Action on Mismatch:** Build fails with clear error message
- **Message Example:** "ERROR: API documentation is out of sync. Run 'mvn clean install' to regenerate."

#### FR4.3 Documented Endpoint Details
Each endpoint must include:
- HTTP Method (GET, POST, PUT, DELETE)
- Endpoint path
- Request parameters (path, query, body)
- Response schema (success and error cases)
- Validation rules
- Example curl commands

---

## 🏗️ Entity Model & Database Schema

### Book Entity

```java
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 13)
    private String isbn;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, length = 100)
    private String author;
    
    @Column(length = 100)
    private String publisher;
    
    @Column(name = "publication_date")
    private LocalDate publicationDate;
    
    @Column(nullable = false, length = 50)
    private String genre;
    
    @Column(name = "quantity_available", nullable = false)
    private Integer quantityAvailable;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### Database Schema (SQL)

```sql
CREATE TABLE books (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(13) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    publisher VARCHAR(100),
    publication_date DATE,
    genre VARCHAR(50) NOT NULL,
    quantity_available INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_isbn (isbn),
    INDEX idx_title (title),
    INDEX idx_genre (genre),
    INDEX idx_author (author)
);
```

---

## 🔌 API Specifications

### Base URL
```
http://localhost:8080/api
```

### Endpoint 1: Get All Books (Paginated)
```
GET /api/books?page=0&size=20
```

**Description:** Retrieve all books with pagination support

**Query Parameters:**
| Parameter | Type | Default | Required | Description |
|---|---|---|---|---|
| page | int | 0 | No | Zero-based page number |
| size | int | 20 | No | Number of books per page |

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Books retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "isbn": "978-3-16-148410-0",
        "title": "Effective Java",
        "author": "Joshua Bloch",
        "publisher": "Addison-Wesley",
        "publicationDate": "2018-01-01",
        "genre": "Programming",
        "quantityAvailable": 5,
        "createdAt": "2026-08-14T10:00:00",
        "updatedAt": "2026-08-14T10:00:00"
      }
    ],
    "totalElements": 150,
    "totalPages": 8,
    "currentPage": 0,
    "pageSize": 20
  },
  "timestamp": 1692007200000
}
```

**Error (500 Internal Server Error):**
```json
{
  "code": 500,
  "message": "Failed to retrieve books",
  "timestamp": 1692007200000
}
```

---

### Endpoint 2: Get Book by ISBN
```
GET /api/books/{isbn}
```

**Description:** Retrieve a specific book by ISBN

**Path Parameters:**
| Parameter | Type | Required | Description |
|---|---|---|---|
| isbn | string | Yes | Book ISBN (10-13 digits) |

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Book retrieved successfully",
  "data": {
    "id": 1,
    "isbn": "978-3-16-148410-0",
    "title": "Effective Java",
    "author": "Joshua Bloch",
    "publisher": "Addison-Wesley",
    "publicationDate": "2018-01-01",
    "genre": "Programming",
    "quantityAvailable": 5,
    "createdAt": "2026-08-14T10:00:00",
    "updatedAt": "2026-08-14T10:00:00"
  },
  "timestamp": 1692007200000
}
```

**Error (404 Not Found):**
```json
{
  "code": 404,
  "message": "Book with ISBN 978-3-16-148410-0 not found",
  "timestamp": 1692007200000
}
```

**Error (400 Bad Request):**
```json
{
  "code": 400,
  "message": "Invalid ISBN format",
  "timestamp": 1692007200000
}
```

---

### Endpoint 3: Create Book
```
POST /api/books
Content-Type: application/json
```

**Description:** Add a new book to the catalog

**Request Body:**
```json
{
  "isbn": "978-3-16-148410-0",
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "publisher": "Addison-Wesley",
  "publicationDate": "2018-01-01",
  "genre": "Programming",
  "quantityAvailable": 5
}
```

**Request Field Validation:**
| Field | Type | Required | Constraints |
|---|---|---|---|
| isbn | string | Yes | 10-13 digits, unique |
| title | string | Yes | 1-200 characters |
| author | string | Yes | 1-100 characters |
| publisher | string | No | Max 100 characters |
| publicationDate | date | No | Must be valid, not in future |
| genre | string | Yes | 1-50 characters |
| quantityAvailable | int | No | >= 0, default = 0 |

**Response (201 Created):**
```json
{
  "code": 201,
  "message": "Book created successfully",
  "data": {
    "id": 1,
    "isbn": "978-3-16-148410-0",
    "title": "Effective Java",
    "author": "Joshua Bloch",
    "publisher": "Addison-Wesley",
    "publicationDate": "2018-01-01",
    "genre": "Programming",
    "quantityAvailable": 5,
    "createdAt": "2026-08-14T10:00:00",
    "updatedAt": "2026-08-14T10:00:00"
  },
  "timestamp": 1692007200000
}
```

**Error (400 Bad Request - Validation Failed):**
```json
{
  "code": 400,
  "message": "Validation failed",
  "errors": {
    "title": "Title is required",
    "isbn": "Invalid ISBN format"
  },
  "timestamp": 1692007200000
}
```

**Error (409 Conflict - Duplicate ISBN):**
```json
{
  "code": 409,
  "message": "ISBN 978-3-16-148410-0 already exists in catalog",
  "timestamp": 1692007200000
}
```

---

### Endpoint 4: Update Book
```
PUT /api/books/{isbn}
Content-Type: application/json
```

**Description:** Update existing book information (partial or full update)

**Path Parameters:**
| Parameter | Type | Required | Description |
|---|---|---|---|
| isbn | string | Yes | Book ISBN (10-13 digits) |

**Request Body (All fields optional, only provided fields are updated):**
```json
{
  "title": "Effective Java (3rd Edition)",
  "quantityAvailable": 10
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Book updated successfully",
  "data": {
    "id": 1,
    "isbn": "978-3-16-148410-0",
    "title": "Effective Java (3rd Edition)",
    "author": "Joshua Bloch",
    "publisher": "Addison-Wesley",
    "publicationDate": "2018-01-01",
    "genre": "Programming",
    "quantityAvailable": 10,
    "createdAt": "2026-08-14T10:00:00",
    "updatedAt": "2026-08-14T11:30:00"
  },
  "timestamp": 1692007200000
}
```

**Error (404 Not Found):**
```json
{
  "code": 404,
  "message": "Book with ISBN 978-3-16-148410-0 not found",
  "timestamp": 1692007200000
}
```

**Error (400 Bad Request - Invalid Update):**
```json
{
  "code": 400,
  "message": "Validation failed",
  "errors": {
    "quantityAvailable": "Quantity must be non-negative"
  },
  "timestamp": 1692007200000
}
```

---

### Endpoint 5: Delete Book
```
DELETE /api/books/{isbn}
```

**Description:** Remove a book from the catalog

**Path Parameters:**
| Parameter | Type | Required | Description |
|---|---|---|---|
| isbn | string | Yes | Book ISBN (10-13 digits) |

**Response (204 No Content):**
```
[Empty Body]
```

**Error (404 Not Found):**
```json
{
  "code": 404,
  "message": "Book with ISBN 978-3-16-148410-0 not found",
  "timestamp": 1692007200000
}
```

---

### Bonus Endpoint 6: Search Books by Title
```
GET /api/books/search?title=Java&page=0&size=20
```

**Description:** Search for books by partial or full title match (case-insensitive)

**Query Parameters:**
| Parameter | Type | Required | Description |
|---|---|---|---|
| title | string | Yes | Search term (partial match supported) |
| page | int | No | Zero-based page number (default: 0) |
| size | int | No | Results per page (default: 20) |

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Search completed successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "isbn": "978-3-16-148410-0",
        "title": "Effective Java",
        "author": "Joshua Bloch",
        "publisher": "Addison-Wesley",
        "publicationDate": "2018-01-01",
        "genre": "Programming",
        "quantityAvailable": 5,
        "createdAt": "2026-08-14T10:00:00",
        "updatedAt": "2026-08-14T10:00:00"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "currentPage": 0,
    "pageSize": 20
  },
  "timestamp": 1692007200000
}
```

---

## 📊 Non-Functional Requirements

### NFR1: Performance
- **Response Time:** API responses should complete within 500ms for 95th percentile
- **Pagination:** Default page size of 20 books (adjustable)
- **Database Indexing:** Indexes on isbn, title, genre, author columns
- **Caching:** Optional caching for frequently accessed books

### NFR2: Scalability
- **Concurrent Users:** Support at least 100 concurrent requests
- **Data Volume:** Capable of handling 100,000+ books in catalog
- **Horizontal Scaling:** Stateless API design for load balancing

### NFR3: Reliability
- **Uptime:** 99.5% availability target
- **Data Integrity:** ACID compliance for database transactions
- **Error Recovery:** Graceful error handling with detailed messages

### NFR4: Security
- **Input Validation:** All inputs validated before processing
- **Error Messages:** No sensitive data in error responses
- **CORS:** Configured for cross-origin requests
- **Logging:** Audit trail of all operations

### NFR5: Maintainability
- **Code Quality:** Follow SOLID principles and clean code guidelines
- **Testing:** Minimum 80% code coverage (backend), 70% (frontend)
- **Documentation:** Auto-generated API docs (Markdown) from code
- **Build Automation:** Maven/npm builds with automated testing

### NFR6: Usability
- **API Simplicity:** RESTful design with intuitive endpoints
- **UI/UX:** Responsive React interface
- **Accessibility:** Semantic HTML, proper error messages
- **Pagination:** User-friendly pagination controls

---

## 🔐 Technology Stack

| Component | Technology | Rationale |
|---|---|---|
| **Backend Framework** | Spring Boot 3.1 | Enterprise-grade, fast setup, mature ecosystem |
| **Language** | Java 17 | Type-safe, robust, excellent for backend |
| **Database** | H2 (Dev) | In-memory, zero-config, perfect for rapid development |
| **Frontend Framework** | React 18 | Modern, component-based, large community |
| **Frontend Language** | TypeScript | Type safety, better IDE support, fewer bugs |
| **Styling** | Tailwind CSS | Utility-first, quick prototyping, responsive |
| **HTTP Client** | Axios | Promise-based, modern, simple API |
| **Testing - Backend** | JUnit 5, Mockito | Industry standard, comprehensive |
| **Testing - Frontend** | React Testing Library | Best practices, user-centric testing |
| **E2E Testing** | Selenium | Browser automation, comprehensive testing |
| **Build Tool - Backend** | Maven | Dependency management, plugin ecosystem |
| **Build Tool - Frontend** | npm/Create React App | Standard toolchain, minimal config |
| **Documentation** | Springdoc-OpenAPI | Auto-generates API docs, keeps in sync |
| **Version Control** | Git | Distributed, collaboration-friendly |

---

## ✅ Acceptance Criteria

### AC1: Book Management Functions
- [ ] User can create a new book with all required fields
- [ ] User can view all books with pagination
- [ ] User can view a specific book by ISBN
- [ ] User can search books by title (case-insensitive, partial match)
- [ ] User can update book information (full or partial)
- [ ] User can delete a book
- [ ] All operations return appropriate HTTP status codes

### AC2: Data Validation
- [ ] ISBN format is validated (10-13 digits)
- [ ] ISBN uniqueness is enforced
- [ ] Required fields (Title, Author, Genre) cannot be empty
- [ ] Field length constraints are enforced
- [ ] Invalid data returns 400 Bad Request with detailed errors
- [ ] Future publication dates are rejected

### AC3: Error Handling
- [ ] 404 error when book not found
- [ ] 409 error when ISBN duplicate attempted
- [ ] 400 error for validation failures
- [ ] All error responses include code, message, and timestamp
- [ ] No stack traces exposed in error responses

### AC4: Auto-Doc Sync
- [ ] /docs/api.md is generated on startup
- [ ] All 6 endpoints documented with full specifications
- [ ] Build fails if /docs/api.md doesn't match current code
- [ ] Documentation includes request/response examples
- [ ] Documentation includes validation rules and error codes

### AC5: Testing & Quality
- [ ] Backend code coverage >= 80%
- [ ] Frontend code coverage >= 70%
- [ ] All unit tests pass (100% pass rate)
- [ ] Integration tests pass (100% pass rate)
- [ ] E2E tests pass (100% pass rate)
- [ ] No hardcoded secrets or credentials

### AC6: Performance & Scalability
- [ ] API response time < 500ms for 95th percentile
- [ ] Supports pagination with default size 20
- [ ] Database queries optimized with indexes
- [ ] Supports at least 100 concurrent requests

### AC7: Security & Compliance
- [ ] CORS configured for frontend origin
- [ ] Input validation on all endpoints
- [ ] SQL injection prevention (parameterized queries)
- [ ] Error messages don't expose sensitive data
- [ ] Audit logging of all operations

### AC8: Documentation & Deployment
- [ ] requirements.md complete and committed
- [ ] API documentation (/docs/api.md) auto-generated
- [ ] README with setup and run instructions
- [ ] Code pushed to feature branches
- [ ] Pull requests created and merged to main
- [ ] Git tags created (v1.0, v1.0-ready-for-prod)

---

## 📝 Assumptions

1. ✅ Single database for all operations (no distributed database)
2. ✅ No user authentication/authorization required (admin-only)
3. ✅ No multi-tenancy required
4. ✅ No real-time synchronization needed
5. ✅ Frontend runs on same or different server (CORS enabled)
6. ✅ H2 database sufficient for MVP (PostgreSQL for production)
7. ✅ ISBN format validation only (no external ISBN lookup)
8. ✅ Soft delete optional (hard delete acceptable)

---

## 🎯 Deliverables Checklist

- [x] requirements.md (complete)
- [x] All Functional Requirements documented (FR1-FR4)
- [x] All Non-Functional Requirements specified
- [x] API specifications for 6 endpoints (with examples)
- [x] Entity model defined
- [x] Database schema provided
- [x] Validation rules documented
- [x] Error handling strategy specified
- [x] Acceptance criteria defined
- [x] Technology stack selected
- [x] Assumptions documented

---

## 📞 Questions & Clarifications Addressed

✅ **Q1: Scope of Operations** → Full CRUD (Create, Read, Update, Delete)  
✅ **Q2: Book Fields** → ISBN, Title, Author, Publisher, Publication Date, Genre, Quantity  
✅ **Q3: Search Capability** → Search by Title only (case-insensitive, partial match)  
✅ **Q4: Auto-Doc Sync** → Markdown format (/docs/api.md), full endpoint details, validated on build  
✅ **Q5: Tech Stack** → Java Spring Boot (Backend), React (Frontend), H2 (Database)  

---

**Status:** ✅ **REQUIREMENTS COMPLETE & READY FOR PHASE 2 - ARCHITECTURE DESIGN**

---

*Last Updated: October 14, 2026*  
*Next Phase: System Architecture Design*