package io.simplehex.map.transport;

import io.simplehex.map.domain.HexCoord;
import io.simplehex.map.domain.TerrainType;

public record AppliedMapCommandResponse(
        String type,
        HexCoord cell,
        TerrainType terrain,
        Boolean terrainHidden,
        Boolean featureHidden,
        String territoryFactionId
) {
}
