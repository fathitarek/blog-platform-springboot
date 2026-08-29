# Code Explanation

This file explains the created project files at a practical level.

## Build and Container Files

### `pom.xml`

- Purpose: defines the Maven build, dependency graph, and Java release target.
- Why it exists: Spring Boot, JPA, security, Flyway, Redis, Swagger, and test dependencies all need central management.
- Dependencies: Spring Boot parent, web, security, validation, data JPA, cache, Redis, Flyway, OpenAPI, JWT, MySQL, H2, Mockito, Testcontainers.
- Important methods/classes: not applicable, but the build plugin config matters.
- Flow: Maven resolves dependencies, compiles sources, and runs tests.
- Pattern: build configuration as infrastructure.
- Future improvement: add Maven Wrapper if you want a pinned developer experience.

### `Dockerfile`

- Purpose: multi-stage build for the application image.
- Why it exists: produces a smaller runtime image and separates build tooling from runtime.
- Dependencies: Maven builder image, JRE runtime image.
- Important steps: build JAR in stage 1, copy it into stage 2, run as a non-root user.
- Flow: source -> Maven package -> runtime container launch.
- Pattern: multi-stage container build.
- Future improvement: add image scanning and SBOM generation.

### `docker-compose.yml`

- Purpose: orchestrates MySQL, Redis, phpMyAdmin, and the API.
- Why it exists: gives a one-command local environment.
- Dependencies: Docker images and the app image.
- Important services:
  - `mysql`
  - `redis`
  - `phpmyadmin`
  - `blog-api`
- Flow: compose starts dependencies first, then the API.
- Pattern: infrastructure composition.
- Future improvement: add separate overrides for development and production.

### `.env.example`

- Purpose: documents the environment variables expected by the stack.
- Why it exists: keeps secrets and credentials out of source code.
- Dependencies: Docker Compose and Spring configuration.
- Future improvement: add separate examples for development and production.

### `.dockerignore`

- Purpose: keeps build context small.
- Why it exists: avoids sending `target`, git metadata, and editor files into Docker builds.
- Future improvement: exclude any large generated artifacts added later.

### `.gitignore`

- Purpose: keeps build outputs and local editor state out of version control.
- Why it exists: avoids committing `target`, IDE files, and local env files.

## Application Configuration

### `src/main/resources/application.yml`

- Purpose: base application settings.
- Why it exists: defines datasource defaults, JPA validation mode, Flyway, cache defaults, JWT settings, and Swagger path.
- Important properties:
  - `spring.jpa.hibernate.ddl-auto=validate`
  - `spring.flyway.enabled=true`
  - `app.jwt.*`
  - `app.cache.redis-enabled`
- Future improvement: split more environment-specific settings into profile files.

### `src/main/resources/application-docker.yml`

- Purpose: Docker-specific datasource and Redis wiring.
- Why it exists: maps the compose service names into Spring Boot settings.
- Future improvement: add observability and logging settings for container deployments.

### `src/main/resources/application-test.yml`

- Purpose: test profile with H2 and a stable JWT secret.
- Why it exists: makes tests self-contained and repeatable.
- Future improvement: add an optional Testcontainers profile if you want MySQL integration tests later.

## Flyway Migrations

### `V1__create_users.sql`

- Purpose: creates the `users` table.
- Why it exists: user accounts are the foundation of auth and ownership.
- Important objects: primary key, unique email index, timestamp columns.

### `V2__create_roles.sql`

- Purpose: creates the `roles` table.
- Why it exists: role-based authorization needs a stable role lookup table.

### `V3__create_user_roles.sql`

- Purpose: creates the join table for many-to-many user-role mapping.
- Why it exists: users can hold multiple roles cleanly.
- Important objects: composite primary key and foreign keys.

### `V4__create_posts.sql`

- Purpose: creates the `posts` table.
- Why it exists: stores blog post content and ownership.
- Important objects: `author_id`, category check constraint, date indexes.

### `V5__create_comments.sql`

- Purpose: creates the `comments` table.
- Why it exists: stores discussion tied to posts and users.
- Important objects: foreign keys and indexing on `post_id` and `user_id`.

### `V6__seed_roles.sql`

- Purpose: inserts the `ADMIN` and `AUTHOR` roles.
- Why it exists: the application expects these roles to be present immediately.

## Bootstrapping and Shared Infrastructure

### `BlogPlatformApplication.java`

- Purpose: Spring Boot main class.
- Important annotations:
  - `@SpringBootApplication`
  - `@EnableCaching`
  - `@EnableAsync`
  - `@EnableJpaAuditing`
  - `@EnableConfigurationProperties(AppProperties.class)`
- Flow: boots the application context and enables core framework features.
- Pattern: composition root.
- Future improvement: add `@EnableScheduling` if future background jobs are needed.

### `config/AppProperties.java`

- Purpose: typed configuration for JWT and cache settings.
- Why it exists: keeps configuration structured and validated.
- Important annotations:
  - `@ConfigurationProperties`
  - `@Validated`
- Future improvement: add more nested settings if the system grows.

### `config/CacheConfig.java`

- Purpose: selects Redis cache management when enabled, otherwise falls back to in-memory cache.
- Why it exists: lets local tests run without Redis while still supporting Redis in Docker.
- Important methods:
  - `redisCacheManager`
  - `simpleCacheManager`
- Pattern: conditional configuration.

### `config/DataInitializer.java`

- Purpose: seeds role rows if they are missing.
- Why it exists: keeps role lookup available even in test or bootstrap environments.
- Important method: `run`.
- Pattern: startup data seeding.

### `config/OpenApiConfig.java`

- Purpose: configures Swagger/OpenAPI metadata and bearer auth scheme.
- Why it exists: documents the API and enables Swagger UI authorization.

## Common Layer

### `common/exception/*`

- `BadRequestException`: generic 400-level business error.
- `DuplicateResourceException`: used for duplicate email registration.
- `ForbiddenOperationException`: used for ownership or permission failures.
- `ResourceNotFoundException`: used for missing posts/comments/users.
- `GlobalExceptionHandler`: converts exceptions into a consistent JSON shape.

Important behavior:

- Validation errors include field-level details.
- Authentication failures return 401.
- Authorization failures return 403.
- Unexpected errors return 500 with a generic message.

Design pattern:

- centralized exception translation.

### `common/response/PageResponse.java`

- Purpose: returns page metadata without exposing Spring Data internals.
- Why it exists: keeps the API response stable and clean.

## User Module

### `user/domain/RoleName.java`

- Purpose: enumerates `ADMIN` and `AUTHOR`.
- Why it exists: avoids magic strings for roles.

### `user/domain/RoleEntity.java`

- Purpose: JPA entity for persisted roles.
- Why it exists: supports many-to-many role assignment.
- Important annotations:
  - `@Entity`
  - `@Enumerated(EnumType.STRING)`
  - unique constraint on `name`

### `user/domain/UserEntity.java`

- Purpose: JPA entity for users.
- Why it exists: stores identity, password, and roles.
- Important relationships:
  - many-to-many `roles`
- Important annotations:
  - `@EntityListeners(AuditingEntityListener.class)`
  - `@CreatedDate`
  - `@LastModifiedDate`
- Future improvement: add account status fields if the product needs disabled/locked accounts.

### `user/repository/UserRepository.java`

- Purpose: loads users by email and exposes basic CRUD.
- Why it exists: auth and ownership checks need user lookup.
- Important behavior: `@EntityGraph(attributePaths = "roles")` loads roles for security.

### `user/repository/RoleRepository.java`

- Purpose: loads roles by `RoleName`.
- Why it exists: registration and seeding need role lookup.

### `user/dto/UserResponse.java`

- Purpose: API-safe user payload for auth responses.
- Why it exists: excludes password and entity internals.

### `user/dto/UserSummaryResponse.java`

- Purpose: lightweight user shape for post and comment payloads.
- Why it exists: keeps nested responses small.

### `user/mapper/UserMapper.java`

- Purpose: converts `UserEntity` to response DTOs.
- Why it exists: keeps mapping out of controllers and services.
- Pattern: mapper pattern.

## Security Module

### `security/SecurityConfig.java`

- Purpose: defines the Spring Security filter chain.
- Why it exists: protects routes, configures stateless JWT auth, and wires exception handlers.
- Important pieces:
  - CSRF disabled for stateless API
  - session policy set to stateless
  - public auth/swagger endpoints
  - JWT filter before `UsernamePasswordAuthenticationFilter`
  - `PasswordEncoder` bean
- Future improvement: add rate limiting and stronger CSP headers at the gateway.

### `security/JwtService.java`

- Purpose: create and validate JWTs.
- Why it exists: centralizes token creation and parsing.
- Important methods:
  - `generateToken`
  - `extractUsername`
  - `extractClaims`
  - `isTokenValid`
- Pattern: service abstraction for token handling.
- Future improvement: add refresh token support if needed.

### `security/JwtAuthenticationFilter.java`

- Purpose: reads Bearer tokens from incoming requests.
- Why it exists: authenticates requests before controller execution.
- Flow:
  - read `Authorization` header
  - validate JWT
  - load user details
  - populate security context

### `security/CustomUserDetailsService.java`

- Purpose: loads users by email for Spring Security.
- Why it exists: enables username/password authentication.

### `security/UserPrincipal.java`

- Purpose: adapts `UserEntity` into `UserDetails`.
- Why it exists: Spring Security works with `UserDetails`.
- Important fields:
  - id
  - name
  - email
  - password
  - authorities

### `security/CurrentUserService.java`

- Purpose: exposes the authenticated user to the service layer.
- Why it exists: ownership checks need the current user id and role.

### `security/OwnershipAuthorizationService.java`

- Purpose: checks whether the current user can modify a post or delete a comment.
- Why it exists: ownership cannot rely on role annotations alone.
- Pattern: dedicated authorization service.

### `security/RestAuthenticationEntryPoint.java`

- Purpose: returns a JSON 401 response for unauthenticated requests.
- Why it exists: API clients should not receive HTML login pages.

### `security/RestAccessDeniedHandler.java`

- Purpose: returns a JSON 403 response for forbidden access.
- Why it exists: keeps API errors consistent.

## Auth Module

### `auth/dto/RegisterRequest.java`

- Purpose: registration payload.
- Why it exists: validates name, email, and password before service entry.

### `auth/dto/LoginRequest.java`

- Purpose: login payload.
- Why it exists: validates credentials before authentication.

### `auth/dto/AuthResponse.java`

- Purpose: login/register response containing token and user info.
- Why it exists: gives clients the JWT and a safe user summary.

### `auth/service/AuthService.java`

- Purpose: registration and login business logic.
- Why it exists: keeps the controller thin and centralizes auth rules.
- Important steps:
  - prevent duplicate email
  - encode password
  - assign AUTHOR role on register
  - authenticate login requests
  - issue JWT
- Pattern: application service.

### `auth/controller/AuthController.java`

- Purpose: exposes `/api/auth/register` and `/api/auth/login`.
- Why it exists: translates HTTP requests into service calls.

## Post Module

### `post/domain/Category.java`

- Purpose: enum of allowed categories.
- Why it exists: enforces valid category values.

### `post/domain/PostEntity.java`

- Purpose: JPA entity for blog posts.
- Why it exists: stores title, content, category, author, and timestamps.
- Important relationships:
  - many-to-one author
  - one-to-many comments
- Important behavior:
  - `update(...)` updates mutable fields in one place

### `post/repository/PostRepository.java`

- Purpose: persists posts and supports specifications.
- Why it exists: search and filtering are best handled via `JpaSpecificationExecutor`.
- Important behavior: `@EntityGraph` loads author and comments for single-post reads.

### `post/dto/PostUpsertRequest.java`

- Purpose: create/update payload for posts.
- Why it exists: validates title, content, and category.

### `post/dto/PostFilterRequest.java`

- Purpose: captures pagination, sorting, filtering, and search query parameters.
- Why it exists: keeps list query parsing consistent.
- Important methods:
  - `resolvedPage`
  - `resolvedSize`
  - `resolvedSortBy`
  - `resolvedSortDirection`
  - `cacheKey`

### `post/dto/PostSummaryResponse.java`

- Purpose: lightweight post response for lists.
- Why it exists: keeps list endpoints efficient.

### `post/dto/PostDetailResponse.java`

- Purpose: detailed post response for single-post reads.
- Why it exists: includes author and comments.

### `post/mapper/PostMapper.java`

- Purpose: converts posts into summary and detailed response DTOs.
- Why it exists: keeps response formatting separate from business logic.

### `post/specification/PostSpecification.java`

- Purpose: dynamic query builder for filtering and search.
- Why it exists: avoids a large number of repository methods.
- Supports:
  - category
  - authorId
  - fromDate
  - toDate
  - search by title, author name, and category

### `post/service/PostService.java`

- Purpose: post CRUD and list operations.
- Why it exists: contains business rules and cache management.
- Important methods:
  - `createPost`
  - `getPost`
  - `listPosts`
  - `updatePost`
  - `deletePost`
  - `getPostEntity`
- Important behaviors:
  - author is always taken from the authenticated user
  - unauthorized post mutation throws 403
  - missing post throws 404
  - cache eviction runs on writes

### `post/controller/PostController.java`

- Purpose: exposes all post endpoints.
- Why it exists: maps HTTP requests to service methods.

## Comment Module

### `comment/domain/CommentEntity.java`

- Purpose: JPA entity for comments.
- Why it exists: stores comment content, commenter, and parent post.

### `comment/repository/CommentRepository.java`

- Purpose: persists comments and loads comments by post.
- Why it exists: supports the post comments endpoint and deletion.

### `comment/dto/CommentCreateRequest.java`

- Purpose: validates comment content.

### `comment/dto/CommentResponse.java`

- Purpose: safe response model for comments.

### `comment/mapper/CommentMapper.java`

- Purpose: converts comment entities into response DTOs.

### `comment/service/CommentService.java`

- Purpose: comment create/list/delete behavior.
- Why it exists: centralizes comment ownership and cache invalidation.
- Important methods:
  - `addComment`
  - `getCommentsByPost`
  - `deleteComment`

### `comment/controller/CommentController.java`

- Purpose: exposes comment endpoints.
- Why it exists: supports comment creation and deletion through REST.

## Tests

### `src/test/java/com/example/blog/auth/service/AuthServiceTest.java`

- Purpose: unit tests for registration and login behavior.
- Why it exists: verifies auth logic without running the entire HTTP stack.

### `src/test/java/com/example/blog/security/OwnershipAuthorizationServiceTest.java`

- Purpose: unit tests for ownership checks.
- Why it exists: ensures the admin/owner rules are enforced.

### `src/test/java/com/example/blog/BlogApiIntegrationTest.java`

- Purpose: MockMvc integration tests for the full API flow.
- Why it exists: verifies registration, login, post CRUD, comments, filtering, pagination, search, and authorization.

