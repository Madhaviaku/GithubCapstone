# Architecture Proposal for the Library Book Management Catalog

## 1. Overview

The system should follow a modular, layered architecture built around a Spring Boot backend and a React frontend. This design supports the required CRUD operations, validates input consistently, provides a clean REST API, and enforces the auto-generated documentation sync requirement without introducing unnecessary complexity.

The architecture is intentionally:

- Simple and maintainable for an MVP
- Stateless and scalable for future load growth
- Explicitly aligned to the requirements for CRUD, validation, pagination, and automated API docs
- Easy to test through isolated service and repository layers

---

## 2. Recommended Architecture Pattern

Recommended pattern: 3-tier layered architecture with a thin frontend and a Java Spring Boot API backend.

- Presentation Layer: React application for administrative catalog management
- Application/Service Layer: Spring Boot controllers, services, validators, DTO mapping, and business logic
- Data Layer: JPA repository + relational database (H2 in development, PostgreSQL-ready for production)
- Documentation Layer: build-time documentation validator that checks generated API docs against the code

This matches the project requirements and keeps responsibilities clean.

---

## 3. High-Level Component Diagram

```mermaid
flowchart LR
    User[Library Administrator] --> UI[React Frontend\nBook Catalog UI]
    UI --> API[Spring Boot REST API\n/controllers]
    API --> SVC[Service Layer\nValidation + Business Rules]
    SVC --> JPA[JPA Repository Layer]
    JPA --> DB[(Relational Database\nH2/PostgreSQL)]

    API --> DOC[OpenAPI/Markdown Docs Generator]
    DOC --> FILE[/docs/api.md]
    FILE --> BUILD[Maven Build Validation]
    BUILD --> CI[Fail Build on Doc Drift]

    SVC --> ERR[Exception Handling\nValidation Errors]
    ERR --> API
    UI --> LOG[Audit Logging / Monitoring]
    LOG --> OBS[Application Logs / Metrics]
```

---

## 4. Key Components and Responsibilities

### 4.1 React Frontend

Responsibilities:

- Render the book catalog dashboard and forms
- Allow create, update, delete, view, and search operations
- Handle pagination controls and validation feedback
- Display success and error messages returned by the API
- Send HTTP requests using Axios to the backend API

Why this fits:

- Matches the requirement for a responsive admin interface
- Keeps the UI decoupled from the datastore and business rules
- Enables future extension for additional book management features

---

### 4.2 REST API Layer (Spring Boot)

Responsibilities:

- Expose REST endpoints for all book operations
- Accept and validate HTTP requests
- Convert request payloads to domain objects or DTOs
- Return standardized JSON responses with status codes and timestamps
- Delegate business logic to the service layer

Endpoints:

- GET /api/books
- GET /api/books/{isbn}
- POST /api/books
- PUT /api/books/{isbn}
- DELETE /api/books/{isbn}
- GET /api/books/search

Design principle:

- Controllers stay thin and focused on transport concerns
- Business rules live in the service layer

---

### 4.3 Service Layer

Responsibilities:

- Enforce validation rules from FR2 and FR3
- Check ISBN uniqueness and field constraints
- Manage pagination/search logic
- Apply business rules such as rejecting future publication dates
- Orchestrate repository calls and error handling

This layer is the core decision point for the application, ensuring the domain rules are consistent across all endpoints.

---

### 4.4 Validation and Exception Handling

Responsibilities:

- Validate ISBN format, required fields, max lengths, and non-negative quantities
- Return structured error responses with detailed field-level errors
- Distinguish validation failures, not-found cases, and duplicate ISBN issues

Recommended handling:

- Custom validation annotations or Bean Validation (`@Valid`, `@NotBlank`, etc.)
- Global exception handler for `MethodArgumentNotValidException`, `EntityNotFoundException`, and duplicate key cases
- Standard response envelope: `code`, `message`, `timestamp`, and optional `errors`

---

### 4.5 Repository Layer

Responsibilities:

- Persist book entities to the database
- Support CRUD and title-based searching
- Optimize lookup by ISBN, title, author, and genre

Recommended implementation:

- Spring Data JPA repositories
- Indexes on `isbn`, `title`, `author`, and `genre`
- Pagination support via `Pageable`

This is the cleanest fit for a catalog system that needs both search and transactional updates.

---

### 4.6 Database Layer

Responsibilities:

- Store persistent book records
- Maintain referential integrity and uniqueness constraints
- Handle transactional writes for create/update/delete operations

Recommended store:

- H2 for local development and testing, as required by the project
- PostgreSQL as the production-grade upgrade path when the app is deployed beyond MVP

Schema design follows the required `books` table with:

- `id`
- `isbn`
- `title`
- `author`
- `publisher`
- `publication_date`
- `genre`
- `quantity_available`
- `created_at`
- `updated_at`

---

### 4.7 Auto-Documentation Sync Component

Responsibilities:

- Inspect the REST API code and generated controller metadata
- Produce a markdown file representing the exposed endpoints, request/response schemas, validation rules, and examples
- Compare generated output to the committed `/docs/api.md`
- Fail the Maven build if docs are stale or missing

Recommended implementation:

- Springdoc OpenAPI or an equivalent annotation-driven doc generator
- Maven plugin or build step to ensure `/docs/api.md` is generated and compared during `validate` or `verify`
- Build failure message clearly instructing developers to regenerate the docs before committing

This directly satisfies FR4 and the enforced documentation requirement.

---

### 4.8 Logging, Monitoring, and Audit Trail

Responsibilities:

- Record create, update, delete, and search operations
- Track validation failures and system exceptions
- Support troubleshooting and operational visibility

Recommended approach:

- Structured application logs for API requests and key events
- Standardized error logging for 4xx/5xx responses
- Optional metrics/tracing in future phases (Prometheus/Grafana or OpenTelemetry)

---

## 5. Data Flow

### Standard Create Book Flow

1. Admin enters book data in the React UI
2. UI submits JSON payload to `POST /api/books`
3. Controller receives request and validates input
4. Service layer checks rules such as ISBN validity and uniqueness
5. Repository persists the new Book entity to the database
6. Service returns a created DTO to the controller
7. Controller returns a 201 response to the frontend
8. UI refreshes the book list

### Read/Search Flow

1. User opens catalog page or searches by title
2. React requests `/api/books` or `/api/books/search`
3. Controller forwards request to the service layer
4. Service applies pagination/search logic
5. Repository executes optimized queries
6. DTOs are returned and serialized to JSON
7. UI renders paginated results

### Documentation Sync Flow

1. Developer modifies controller, DTOs, or request validation logic
2. Application starts or Maven build runs
3. OpenAPI or controller scanner extracts endpoint metadata
4. Documentation generator writes or refreshes `/docs/api.md`
5. Validation step compares the generated file to the committed version
6. Build fails if they differ, forcing the developer to regenerate and commit the docs

---

## 6. Technology Choices and Why

| Component | Recommendation | Reason |
|---|---|---|
| Backend | Java 17 + Spring Boot 3.1 | Mature enterprise framework, strong ecosystem, good for REST APIs and validation |
| Frontend | React 18 + TypeScript | Component-based UI, strong development tooling, type safety |
| API Contract | REST + JSON | Simple, standard, easy to integrate with UI and docs |
| Persistence | Spring Data JPA + Hibernate | Clean object mapping and easy repository-based CRUD |
| Database | H2 in development; PostgreSQL in production | H2 is fast and zero-config for local work; PostgreSQL is robust for production |
| Validation | Bean Validation + custom validators | Centralized, reusable, and consistent with API requirements |
| Docs | Springdoc OpenAPI + markdown generation | Keeps code and docs synchronized automatically |
| Build | Maven | Required by project setup and ideal for plugin-based doc validation |
| Testing | JUnit 5 + Mockito + React Testing Library | Standard and highly effective for backend/frontend validation |
| HTTP Client | Axios | Simple and familiar for React API calls |

---

## 7. Deployment Topology

### MVP / Local Development

- Frontend served from a local React dev server
- Backend runs as a Spring Boot application on port 8080
- Database is H2 in local memory or file-based mode
- Doc validation happens during Maven build lifecycle

### Production-style Deployment

- Frontend deployed behind a static hosting layer or web application server
- Backend deployed as a stateless application instance
- Database hosted in a managed relational service such as PostgreSQL
- Reverse proxy or API gateway can sit in front of the backend for routing, CORS, and security
- Logs and metrics collected centrally for support and monitoring

This design supports the NFR of horizontal scaling because the API is stateless and the database is the sole shared state.

---

## 8. Architectural Trade-offs

### Chosen Approach: Spring Boot + React + JPA

Benefits:

- Clear separation of concerns
- Fast implementation for CRUD-heavy systems
- Good fit for validation-heavy and documentation-heavy requirements
- Easy to unit test and maintain

Trade-offs:

- Stateless API and database still require careful transaction design
- React frontend adds client-side state management complexity if the UI grows beyond the MVP
- Documentation automation must be maintained carefully with build gating

---

## 9. Alignment with Requirements

This architecture satisfies the key constraints from the requirements document:

- CRUD operations: handled by API controllers + service + repository layers
- Validation: centralized in service and validation components
- Pagination: repository/service support with `Pageable`
- Search: repository query by title with case-insensitive matching
- Auto-doc sync: OpenAPI + build-time validation component
- Error handling: standardized API response model with exceptions mapped to HTTP status codes
- Scalability: stateless backend architecture with database indexing and future horizontal scaling

---

## 10. Design Review Outcome

A structured design review identified the following priority items for architecture hardening before implementation:

- Define the admin-only access assumption explicitly, even if auth is deferred beyond the MVP
- Formalize the doc-generation contract so the Maven build fails only on true drift between `/docs/api.md` and the live API metadata
- Add a migration-based database strategy to reduce H2/PostgreSQL drift risk
- Strengthen the validation and error envelope contract across all endpoints
- Clarify audit logging and operational observability requirements

These are not major redesigns; they are implementation guardrails that keep the architecture aligned with the requirements and reduce production risk.

---

## 11. Recommended Final Architecture Summary

The recommended system is a modular, layered application with:

- React frontend for admin operations
- Spring Boot REST API as the business logic and validation layer
- JPA repository layer with relational database persistence
- Auto-generated API documentation enforced through Maven validation
- Structured logging and exception-driven API responses
- Clear build and migration guardrails before production deployment

This design is simple enough for the MVP, robust enough for real-world catalog operations, and well aligned with the project’s functional and non-functional requirements.
