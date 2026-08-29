# Database Design

## Tables

### `users`

Stores application users and credentials.

### `roles`

Stores role names such as `ADMIN` and `AUTHOR`.

### `user_roles`

Join table for the many-to-many relationship between users and roles.

### `posts`

Stores blog posts and their author relationship.

### `comments`

Stores comments linked to posts and users.

## Relationships

- A user has many roles.
- A user can author many posts.
- A post can have many comments.
- A comment belongs to one user and one post.

## Indexing

Important indexes include:

- user email lookup
- post author lookup
- post category filtering
- post creation time filtering
- comment post lookup

## Constraints

- Emails are unique.
- Role names are unique.
- Categories are restricted to enum values.
- Foreign keys enforce relational integrity.

## Migration Strategy

Flyway manages schema creation in versioned scripts.

## Notes

The schema is intentionally simple and normalized so it can evolve cleanly as the application grows.

