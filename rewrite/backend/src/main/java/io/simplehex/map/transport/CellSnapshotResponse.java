package io.simplehex.map.transport;

import io.simplehex.map.domain.TerrainType;

public record CellSnapshotResponse(
        int q,
        int r,
        TerrainType terrain,
        boolean terrainHidden,
        boolean featureHidden,
        String territoryFactionId
) {
}
