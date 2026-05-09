package io.simplehex.map.application;

import io.simplehex.map.domain.HexCoord;
import io.simplehex.map.domain.TerrainType;

public record AppliedMapCommand(
        String type,
        HexCoord cell,
        TerrainType terrain,
        Boolean terrainHidden,
        Boolean featureHidden,
        String territoryFactionId
) {
}
