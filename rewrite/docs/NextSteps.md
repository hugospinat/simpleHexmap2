# Next Steps

This document captures the next implementation steps after the current terrain and visibility slice.

## 1. Frontend Structure Cleanup

- Reorganize the frontend by feature slice instead of technical buckets alone.
- Target structure: `map/application`, `map/transport`, `map/render`, `map/ui`.
- Move the current terrain/visibility hooks and transport DTOs into the map slice to reduce cross-folder drift.

## 2. Feature Visibility Slice

- Extend the current visibility model from `terrainHidden` to `featureHidden`.
- Add command transport, backend application logic, persistence updates, realtime propagation, and optimistic reconciliation.
- Update the Pixi preview so hidden features and hidden terrain are rendered distinctly.

## 3. Factions And Territories Slice

- Introduce faction ownership for cells with clear authoritative write rules.
- Define the first territory command set and snapshot shape before implementation.
- Keep the same HTTP command plus WebSocket reconciliation model already used for terrain.

## 4. Identity And Session Model

- Replace the temporary role toggle with a real session model.
- Define GM/player identity, map membership, and server-side authorization boundaries.
- Ensure websocket session registration is bound to authenticated actor context rather than a raw query parameter alone.

## 5. Persistence Hardening

- Add explicit migration tooling for production persistence instead of relying on Hibernate schema evolution alone.
- Keep JPA entity definitions as the canonical model, but move durable schema changes into versioned migrations.
- Add coverage for idempotency, operation-log ordering, and persistence restart behavior.

## 6. Validation Expansion

- Add focused tests for role authorization, duplicate `operationId` replay, and visibility edge cases.
- Add a backend smoke path for startup without preexisting data.
- Add a frontend slice test layer for optimistic updates and realtime reconciliation.

## Delivery Order

1. Frontend structure cleanup.
2. `featureHidden` end-to-end slice.
3. Factions and territories domain slice.
4. Session and authorization model.
5. Persistence hardening and broader automated validation.