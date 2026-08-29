# Testing

## Test Types

### Unit Tests

- `AuthServiceTest`
- `OwnershipAuthorizationServiceTest`

These verify business rules and token creation behavior.

### Integration Tests

- `BlogApiIntegrationTest`

This exercises the API through MockMvc:

- register
- login
- create post
- update permissions
- comments
- filtering
- pagination
- search
- unauthorized access

## Profiles

Tests run with the `test` profile.

That profile uses:

- H2 in-memory database
- `ddl-auto=create-drop`
- Flyway disabled

## Running Tests

```bash
mvn test
```

## Test Data Strategy

- Test cases create their own users and posts.
- Roles are seeded at startup by the application initializer.
- Each test clears the repository state it depends on.

