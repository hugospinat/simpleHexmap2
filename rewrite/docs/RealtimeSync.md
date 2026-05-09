# Realtime Sync

## Session model

The backend is authoritative.

Client responsibilities:

- render the last authoritative state
- stage local optimistic commands
- reconcile pending commands when authoritative events arrive

Server responsibilities:

- validate each command
- assign a per-map sequence
- persist the operation log and current projection atomically through JPA
- emit authoritative updates in sequence order

## First slice flow

```text
frontend boot
-> HTTP snapshot fetch for fast initial paint
-> WebSocket connect to /api/maps/{mapId}/ws?role=gm|player
-> server sends sync_snapshot
-> client submits set_cell_terrain or set_cell_visibility over HTTP
-> server validates and sequences
-> server persists operation + current cell projection
-> server emits command_applied to GM sessions
-> server emits sync_snapshot to player sessions
-> client matches `operationId`, clears its pending command, and patches local terrain or visibility state from the authoritative event
```

## Implemented transport surfaces

- `GET /api/maps/{mapId}?role=gm|player`
- `POST /api/maps/{mapId}/commands`
- `WS /api/maps/{mapId}/ws?role=gm|player`

The current frontend still keeps an explicit HTTP refresh action as a fallback, but the normal terrain mutation flow no longer reloads the snapshot after each command.

The current frontend also exposes a local GM/player role switch so the visibility filter can be observed directly against the same backend slice.

## Reconnect policy

- if the client resume point is still available, replay from the next sequence
- otherwise send a fresh `sync_snapshot`

## Idempotency

- every client command includes `operationId`
- the server ignores duplicate `(mapId, operationId)` submissions after the first successful apply