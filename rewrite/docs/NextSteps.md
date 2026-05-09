# Next Steps

This document captures the next implementation steps after the current terrain, visibility, feature visibility, and territory slices.

## Completed slices

- frontend structure cleanup
- feature visibility end-to-end slice
- factions and territories end-to-end slice

## 1. Identity And Session Model

- Replace the temporary role toggle with a real session model.
- Define GM/player identity, map membership, and server-side authorization boundaries.
- Ensure websocket session registration is bound to authenticated actor context rather than a raw query parameter alone.

## 2. Persistence Hardening

- Add explicit migration tooling for production persistence instead of relying on Hibernate schema evolution alone.
- Keep JPA entity definitions as the canonical model, but move durable schema changes into versioned migrations.
- Add coverage for idempotency, operation-log ordering, and persistence restart behavior.

## 3. Validation Expansion

- Add focused tests for role authorization, duplicate `operationId` replay, and visibility edge cases.
- Add a backend smoke path for startup without preexisting data.
- Add a frontend slice test layer for optimistic updates and realtime reconciliation.

## Delivery Order

1. Session and authorization model.
2. Persistence hardening.
3. Broader automated validation.
