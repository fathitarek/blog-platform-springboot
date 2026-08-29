# System Design

This document describes how the current modular monolith could evolve into a larger production system.

## Current Shape

```text
Client
  |
Load Balancer
  |
+------------------------+
| Spring Boot API        |
| Spring Boot API        |
| Spring Boot API        |
+------------------------+
   |                 |
 Redis             MySQL
   |                 |
   +-----------+-----+
               |
           Read Replica
```

## Scaling Path

### API Layer

- Run multiple Spring Boot instances behind a load balancer.
- Keep JWT stateless so instances stay horizontally scalable.
- Use sticky sessions only if a future feature truly needs them.

### Data Layer

- Use MySQL primary for writes.
- Add read replicas for heavy read traffic.
- Index search and filter columns carefully.
- Consider sharding only if the dataset and traffic justify it.

### Caching

- Keep Redis as the shared cache layer.
- Cache post details and list queries.
- Invalidate caches on write operations.

### Async Processing

- Introduce Kafka or RabbitMQ for notifications, email, audit trails, and heavy background work.
- Keep request/response flows fast by offloading non-critical tasks.

### Storage and Media

- If image or file uploads are added, move them to object storage such as S3-compatible storage.
- Put a CDN in front of public static assets.

### Observability

- Centralized logs
- Metrics and alerts
- Distributed tracing
- Correlation IDs per request

### Reliability

- Rate limiting
- Backups
- Health checks
- Graceful shutdown
- Retry policies for async work

## When to Split Services

Split out services only when the boundaries become real operational concerns:

- Auth Service when auth rules and token lifecycle need independent release cadence.
- Post Service when post traffic dominates and needs separate scaling.
- Comment Service when comment volume or moderation flows become distinct.
- Notification Service when email/push/event processing becomes asynchronous and high volume.

