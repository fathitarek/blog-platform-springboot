# API Reference

## Base URL

All endpoints live under `/api`.

## Authentication

- `POST /api/auth/register`
- `POST /api/auth/login`

## Posts

- `POST /api/posts`
- `GET /api/posts`
- `GET /api/posts/{id}`
- `PUT /api/posts/{id}`
- `DELETE /api/posts/{id}`
- `GET /api/posts/{id}/comments`

## Comments

- `POST /api/posts/{postId}/comments`
- `DELETE /api/comments/{id}`

## Query Parameters for `GET /api/posts`

- `page`
- `size`
- `sortBy`
- `sortDirection`
- `category`
- `authorId`
- `fromDate`
- `toDate`
- `search`

## Response Shapes

- Auth endpoints return token plus user info.
- Post list endpoints return `PageResponse`.
- Post detail endpoints include nested author and comments.
- Comment endpoints return the comment payload and author summary.

## Error Format

Errors use a shared JSON body with:

- timestamp
- status
- error
- message
- path
- fieldErrors

