# Security

## Authentication Flow

1. A user registers or logs in.
2. The API issues a JWT.
3. The client sends the token as `Authorization: Bearer <token>`.
4. `JwtAuthenticationFilter` validates the token.
5. Spring Security populates the authentication context.

## Password Hashing

- Passwords are encoded with BCrypt.
- Plaintext passwords are never stored.

## Authorization

- Route-level access is configured in `SecurityConfig`.
- Method security is enabled.
- Ownership checks are enforced in the service layer, not only by annotations.

## Ownership Rules

- Post edits and deletes are allowed only for the author or an admin.
- Comment deletes are allowed for the comment owner or an admin.

## Token Expiration

- Token lifetime is configured with `JWT_EXPIRATION_MINUTES`.
- Expired tokens are rejected.

## Security Considerations

- Keep JWT secrets out of source control.
- Use HTTPS in real deployments.
- Rotate secrets when needed.
- Consider refresh tokens if the product needs longer-lived sessions.
- Add rate limiting for login endpoints in a real production environment.

