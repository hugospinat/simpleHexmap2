# simpleHexmap

This directory contains the clean-room implementation workspace for simpleHexmap.

Current status:

- docs-first architecture pack started
- frontend terrain and visibility slices are wired to the backend over HTTP and WebSocket, with GM/player role switching
- backend terrain and visibility slices are persisted through Spring Data JPA and validated with Spring integration tests under Java 25 and Gradle 9.1

## Layout

- `docs/` architecture and contract documents for simpleHexmap
- `frontend/` React + Pixi application shell
- `backend/` Spring Boot application skeleton

## First implementation slice

The current implemented slice proves the full path for terrain editing and terrain visibility:

- load a map snapshot
- submit a terrain or visibility command
- assign a server sequence
- persist the result through JPA into SQL tables
- broadcast an authoritative update
- reconcile on the frontend with `operationId`
- redraw the terrain layer

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