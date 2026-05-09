# Next Steps

This document captures the next implementation steps after the current terrain, visibility, feature visibility, territory, and session slices.

## Completed slices

- frontend structure cleanup
- feature visibility end-to-end slice
- factions and territories end-to-end slice
- identity and session model slice

## 1. Persistence Hardening

- Add explicit migration tooling for production persistence instead of relying on Hibernate schema evolution alone.
- Keep JPA entity definitions as the canonical model, but move durable schema changes into versioned migrations.
- Add coverage for idempotency, operation-log ordering, and persistence restart behavior.

## 2. Validation Expansion

- Add focused tests for role authorization, duplicate `operationId` replay, and visibility edge cases.
- Add a backend smoke path for startup without preexisting data.
- Add a frontend slice test layer for optimistic updates and realtime reconciliation.

## Delivery Order

1. Persistence hardening.
2. Broader automated validation.
