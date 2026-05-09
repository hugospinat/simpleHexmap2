# Product Scope

## Goal

Deliver a greenfield rewrite of the collaborative hex map editor with near-feature parity and an authoritative realtime backend.

## Included in V1

- authentication and session-backed access control
- workspaces, members, and per-map editing roles
- authoritative map editing over WebSocket
- GM and player filtered map snapshots
- terrain editing
- fog of war
- exclusive faction territories
- roads and rivers
- features
- notes
- tokens

## Explicit V1 decisions

- conflict model: server sequencing with last-write-wins
- territory model: one faction per cell
- token rules: the GM can move all tokens; a player can move only their own token; token moves are allowed only onto visible cells
- import and export are out of scope for the first implementation wave

## Excluded for now

- offline mode
- native mobile UX
- plugin system
- microservice split
- legacy import or export compatibility

## First vertical slice

The first slice must validate the end-to-end architecture with only terrain editing:

1. load a map snapshot
2. send a terrain command
3. apply it on the server in sequence order
4. persist the new state
5. return an authoritative update
6. reconcile and redraw only the terrain layer on the client