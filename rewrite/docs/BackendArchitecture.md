# Backend Architecture

This document defines the intended Spring backend boundaries for the rewrite workspace and the naming rules used to keep those boundaries explicit.

## Layer order

The backend follows this dependency direction:

`transport -> application -> domain -> persistence/realtime/session infrastructure`

- `transport` owns HTTP and WebSocket contract shapes plus mapping to and from application models
- `application` owns use-case orchestration, authorization decisions, sequencing, and transactional flow
- `domain` owns reusable business concepts such as coordinates, roles, and terrain values
- `persistence`, `realtime`, and `session` act as infrastructure adapters around the application layer

## Naming rules

### Spring-facing types

- HTTP entrypoints use `Controller`
- WebSocket/configuration entrypoints use `Handler`, `Interceptor`, or `Config`
- startup/bootstrap components use `Bootstrap` or `Config`

### Application layer

- use-case orchestrators use `Service`
- application events use `Event`
- application models avoid HTTP-specific `Request` or `Response` suffixes

### Domain layer

- domain types use business names such as `HexCoord`, `TerrainType`, and `ActorRole`
- domain types do not use persistence or transport suffixes such as `Entity`, `Request`, `Response`, or `Dto`

### Transport layer

- HTTP and WebSocket contract records use `Request` and `Response`
- transport mapping stays in the transport layer
- transport records do not leak JPA entities or persistence records

### Persistence layer

- JPA rows use `Entity`
- embedded ids use explicit id names such as `MapCellId`
- repository adapters use repository-focused names instead of technology-first names when there is no matching port abstraction

## Current rewrite implications

- `MapController` should validate and map transport requests, then call application services
- `MapService` should work with application commands and application result models rather than transport DTOs
- the persistence adapter should return persistence or application-facing records, never transport responses
- realtime notifications should map application events into transport payloads at the adapter edge
