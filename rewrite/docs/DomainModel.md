# Domain Model

## Aggregate root

`MapAggregate` is the transaction boundary for map editing commands.

Each command:

- targets exactly one map
- is applied in a server-assigned order
- either succeeds atomically or fails without side effects
- records the resulting authoritative sequence

## Core value objects

```text
Coord(q, r)
HexEdge(coord, side)
MapRevision(sequence)
ActorRole(owner | gm | player)
```

## Core entities

```text
MapAggregate
  id
  workspaceId
  revision
  cells
  edges
  factions
  features
  tokens

CellState
  coord
  terrain
  terrainHidden
  featureHidden
  territoryFactionId?
  note?

TokenState
  userId
  position?
```

## Invariants

- each cell is uniquely identified by `(mapId, q, r)`
- each cell has exactly one terrain value
- each cell has at most one territory faction in V1
- a token belongs to exactly one user inside one map
- a player can move only their own token
- a player token move must target a visible cell
- the GM can move any token
- edge commands only target valid neighboring cells

## Command catalog for the first slice

### `SetCellTerrain`

Input:

- `mapId`
- `q`
- `r`
- `terrain`
- `operationId`
- `actorRole`

Behavior:

- creates the cell if the product later supports sparse maps; for the first slice assume the cell already exists
- updates only the terrain field
- increments the authoritative revision through server sequencing
- emits an authoritative terrain delta
- returns the authoritative `operationId` so the client can clear its local pending command

Failure cases:

- map does not exist
- actor is not allowed to edit the map
- target cell does not exist
- terrain value is invalid

## Conflict model

V1 uses server sequencing with last-write-wins.

- clients send intent with an idempotent `operationId`
- the server assigns sequence numbers
- the operation log is authoritative
- later valid operations replace earlier values when they touch the same field