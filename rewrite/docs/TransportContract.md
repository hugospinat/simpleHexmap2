# Transport Contract

## Transport principles

- transport DTOs do not expose persistence rows
- transport DTOs are stable and versioned
- GM and player payloads are filtered server-side
- client commands carry intent, not direct persistence diffs

## First slice snapshot DTO

```json
{
  "mapId": "uuid",
  "revision": 12,
  "role": "gm",
  "cells": [
    {
      "q": 0,
      "r": 0,
      "terrain": "plains",
      "terrainHidden": false,
      "featureHidden": false
    }
  ]
}
```

## First slice command DTOs

```json
{
  "type": "set_cell_terrain",
  "operationId": "uuid",
  "actorRole": "gm",
  "cell": { "q": 0, "r": 0 },
  "terrain": "forest"
}
```

```json
{
  "type": "set_cell_visibility",
  "operationId": "uuid",
  "actorRole": "gm",
  "cell": { "q": 0, "r": 0 },
  "terrainHidden": true
}
```

## First slice server events

WebSocket endpoint:

```text
/api/maps/{mapId}/ws?role=gm|player
```

### `sync_snapshot`

Sent on connect or when the client must resync from authority.

```json
{
  "type": "sync_snapshot",
  "snapshot": {
    "mapId": "uuid",
    "revision": 12,
    "role": "gm",
    "cells": []
  }
}
```

### `command_applied`

```json
{
  "type": "command_applied",
  "operationId": "uuid",
  "mapId": "uuid",
  "sequence": 13,
  "command": {
    "type": "set_cell_terrain",
    "cell": { "q": 0, "r": 0 },
    "terrain": "forest"
  }
}
```

The same `command_applied` envelope is also used for `set_cell_visibility` with `terrainHidden` instead of `terrain`.

### `command_rejected`

```json
{
  "type": "command_rejected",
  "operationId": "uuid",
  "reason": "cell_not_found"
}
```

## Player filtering rule for the first slice

- hidden cells are absent from player payloads when the visibility model requires concealment
- GM payloads include full terrain visibility fields
- GM and owner WebSocket sessions receive `command_applied`
- player WebSocket sessions receive filtered `sync_snapshot` refreshes instead of hidden-capable command deltas