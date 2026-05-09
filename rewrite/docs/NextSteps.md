# Next Steps

This document captures the next implementation steps after the current terrain, visibility, feature visibility, territory, and session slices.

## Completed slices

- frontend structure cleanup
- feature visibility end-to-end slice
- factions and territories end-to-end slice
- identity and session model slice

## 1. Architecture and Naming Alignment

- Keep the docs pack ahead of implementation by updating backend boundary and naming guidance before new slices.
- Remove remaining transport-model coupling from the application and persistence layers.
- Keep startup/bootstrap, transport, application, and infrastructure naming aligned with Spring conventions.

## 2. Persistence Hardening

- Add explicit migration tooling for production persistence instead of relying on Hibernate schema evolution alone.
- Keep JPA entity definitions as the canonical model, but move durable schema changes into versioned migrations.
- Add coverage for idempotency, operation-log ordering, and persistence restart behavior.

## 3. Validation Expansion

- Add focused tests for role authorization, duplicate `operationId` replay, and visibility edge cases.
- Add a backend smoke path for startup without preexisting data.
- Add a frontend slice test layer for optimistic updates and realtime reconciliation.

## Delivery Order

1. Architecture and naming alignment.
2. Persistence hardening.
3. Broader automated validation.
