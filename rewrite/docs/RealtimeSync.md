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
-> GET /api/session to resolve or create a demo actor session
-> HTTP snapshot fetch for fast initial paint
-> WebSocket connect to /api/maps/{mapId}/ws with the authenticated session cookie
-> server sends sync_snapshot
-> client submits set_cell_terrain, set_cell_visibility, set_cell_feature_visibility, or set_cell_territory over HTTP
-> server validates and sequences
-> server persists operation + current cell projection
-> server emits command_applied to GM sessions
-> server emits sync_snapshot to player sessions
-> client matches `operationId`, clears its pending command, and patches local terrain, visibility, feature, or territory state from the authoritative event
```

## Implemented transport surfaces

- `GET /api/session`
- `POST /api/session/actors/{actorId}`
- `GET /api/maps/{mapId}`
- `POST /api/maps/{mapId}/commands`
- `WS /api/maps/{mapId}/ws`

The current frontend still keeps an explicit HTTP refresh action as a fallback, but the normal terrain, visibility, feature visibility, and territory mutation flow no longer reloads the snapshot after each command.

The current frontend now resolves a cookie-backed demo actor session and can switch between seeded GM/player identities so the visibility filter, feature visibility preview, and territory preview can be observed against server-enforced authorization.

## Reconnect policy

- if the client resume point is still available, replay from the next sequence
- otherwise send a fresh `sync_snapshot`

## Idempotency

- every client command includes `operationId`
- the server ignores duplicate `(mapId, operationId)` submissions after the first successful apply
