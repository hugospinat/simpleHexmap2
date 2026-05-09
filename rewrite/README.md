# simpleHexmap

This directory contains the clean-room implementation workspace for simpleHexmap.

Current status:

- docs-first architecture pack started
- frontend terrain, visibility, feature visibility, and territory slices are wired to the backend over HTTP and WebSocket, with GM/player role switching
- backend terrain, visibility, feature visibility, and territory slices are persisted through Spring Data JPA and validated with Spring integration tests under Java 21 and Gradle 9.1

## Layout

- `docs/` architecture and contract documents for simpleHexmap
- `frontend/` React + Pixi application shell
- `backend/` Spring Boot application skeleton

## Current implementation slice

The current implemented slice proves the full path for terrain editing, visibility, feature visibility, and territory ownership:

- load a map snapshot
- submit a terrain, visibility, feature-visibility, or territory command
- assign a server sequence
- persist the result through JPA into SQL tables
- broadcast an authoritative update
- reconcile on the frontend with `operationId`
- redraw the preview with distinct hidden-terrain, hidden-feature, and faction-territory states

## Validation

Frontend:

```bash
cd frontend
npm install
npm run build
npm run dev
```

Backend:

```bash
cd backend
./gradlew test
./gradlew bootRun
```

For local development, run the backend on `localhost:8080` and the frontend dev server on `localhost:4173`. The Vite proxy forwards both HTTP and WebSocket `/api` traffic to the backend.
