package io.simplehex.map.persistence;

import io.simplehex.map.domain.TerrainType;

public record MapCellRecord(
        int q,
        int r,
        TerrainType terrain,
        boolean terrainHidden,
        boolean featureHidden,
        String territoryFactionId
) {
}
