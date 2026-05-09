package io.simplehex.map.application;

import io.simplehex.map.domain.TerrainType;

public record MapCellView(
        int q,
        int r,
        TerrainType terrain,
        boolean terrainHidden,
        boolean featureHidden,
        String territoryFactionId
) {
}
