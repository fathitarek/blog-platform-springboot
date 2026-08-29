# Architecture

## Overview

The project follows a modular monolith structure with clear package separation:

- `auth`
- `comment`
- `common`
- `config`
- `post`
- `security`
- `user`

Each module keeps its own controller, service, repository, DTO, mapper, and domain types.

## Layering

### Controller Layer

Handles HTTP requests and validation. Controllers stay thin and delegate business work to services.

### Service Layer

Contains business rules, authorization checks, cache management, and orchestration.

### Repository Layer

Uses Spring Data JPA repositories for persistence and filtering.

### Domain Layer

Contains JPA entities and enums that define the data model.

### Mapper Layer

Converts entities into API response DTOs.

## Cross-Cutting Concerns

### Security

JWT-based stateless authentication is handled in `security`.

### Caching

Post reads are cached, and write operations evict affected cache regions.

### Error Handling

`GlobalExceptionHandler` standardizes API errors.

### Auditing

JPA auditing tracks created and updated timestamps across entities.

## Data Flow

1. Request enters controller.
2. Validation runs.
3. Service resolves the current user and applies authorization rules.
4. Repository loads or stores data.
5. Mapper converts entities to response DTOs.
6. Controller returns the response.

## Design Choices

- Use `JpaSpecificationExecutor` for flexible post filtering.
- Keep ownership checks in a dedicated service.
- Use enums for categories and roles.
- Keep auth responses separate from user entity representations.

