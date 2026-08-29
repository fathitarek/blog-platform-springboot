# Blog Platform REST API

Production-ready Blog Platform API built with Java 21, Spring Boot, JWT authentication, Spring Data JPA, Flyway, MySQL, Redis caching, Swagger/OpenAPI, and Docker.

## Overview

This project is a modular monolith that provides:

- User registration and login
- JWT-based authentication
- ADMIN and AUTHOR roles
- Blog post CRUD
- Comments
- Search, filtering, sorting, and pagination
- Validation and centralized error handling
- Caching with Spring Cache and Redis in Docker
- Swagger UI for API exploration
- Production-style Docker Compose environment
- Automated tests

## Architecture

The code is organized by feature:

- `auth` for authentication
- `user` for user and role data
- `post` for posts and post search logic
- `comment` for comments
- `security` for JWT and Spring Security
- `common` for shared exceptions and responses

The application exposes DTOs at the API boundary and keeps JPA entities internal to the service layer.

## Technology Stack

- Java 21
- Spring Boot 3.3.x
- Spring Web
- Spring Security
- Spring Data JPA / Hibernate
- Flyway
- MySQL 8
- Redis
- Swagger / OpenAPI
- Maven
- JUnit 5
- Mockito
- Testcontainers dependency included
- Docker and Docker Compose

## Prerequisites

- Java 21
- Maven 3.9+
- Docker Desktop or Docker Engine

## Configuration

Environment variables are defined in `.env.example`.

Important variables:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `JWT_SECRET`
- `JWT_ISSUER`
- `JWT_EXPIRATION_MINUTES`

The application uses `spring.jpa.hibernate.ddl-auto=validate` in production and Flyway for schema creation.

## Run Locally

### With Maven

```bash
mvn test
mvn spring-boot:run
```

You need a MySQL instance available if you run the app outside Docker.

### With Docker Compose

```bash
docker compose build
docker compose up -d
docker compose ps
docker compose logs
```

### Stop Containers

```bash
docker compose down
```

To remove volumes too:

```bash
docker compose down -v
```

## URLs

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- phpMyAdmin: `http://localhost:8081`

## Database Migrations

Flyway migrations live in `src/main/resources/db/migration`.

Migration order:

- `V1__create_users.sql`
- `V2__create_roles.sql`
- `V3__create_user_roles.sql`
- `V4__create_posts.sql`
- `V5__create_comments.sql`
- `V6__seed_roles.sql`

## API Endpoints

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`

### Posts

- `POST /api/posts`
- `GET /api/posts`
- `GET /api/posts/{id}`
- `PUT /api/posts/{id}`
- `DELETE /api/posts/{id}`
- `GET /api/posts/{id}/comments`

### Comments

- `POST /api/posts/{id}/comments`
- `DELETE /api/comments/{id}`

## JWT Flow

1. Register or login.
2. The API returns a JWT token.
3. Send the token in the `Authorization: Bearer <token>` header.
4. Spring Security validates the token in `JwtAuthenticationFilter`.
5. The authenticated user becomes available to the service layer.

## Role Permissions

- `ADMIN`
  - Can create, read, update, and delete any post
  - Can delete any comment
- `AUTHOR`
  - Can create posts
  - Can update and delete only their own posts
  - Can create comments
  - Can delete their own comments

## Example Requests

### Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","password":"password123"}'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"password123"}'
```

### Create Post

```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Spring Boot Basics","content":"Hello","category":"TECHNOLOGY"}'
```

### Filter Posts

```bash
curl "http://localhost:8080/api/posts?category=TECHNOLOGY&search=spring&page=0&size=20&sortBy=createdAt&sortDirection=desc"
```

## Caching Strategy

- Individual post details are cached.
- Post lists are cached by filter/sort/pagination key.
- Cache entries are evicted when posts or comments change.
- Redis is enabled in the Docker profile.

## Testing

Run all tests:

```bash
mvn test
```

Tests included:

- Unit tests for authorization and auth behavior
- MockMvc integration tests for register/login/posts/comments/search/filtering/pagination

## Project Structure

```text
src/main/java/com/example/blog
  auth/
  comment/
  common/
  config/
  post/
  security/
  user/

src/main/resources
  application.yml
  application-docker.yml
  application-test.yml
  db/migration/

docs/
```

## Troubleshooting

- If the app fails to start, check your MySQL credentials and the `JWT_SECRET`.
- If Docker build fails, ensure Docker has network access to pull base images.
- If Swagger UI does not load, verify the app is running on port `8080`.
- If auth returns `401`, make sure the Bearer token is present and not expired.
