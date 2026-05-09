package io.simplehex.map.transport;

import io.simplehex.map.domain.TerrainType;

public record AppliedMapCommandDto(
        String type,
        CellRefDto cell,
        TerrainType terrain,
        Boolean terrainHidden,
        Boolean featureHidden,
        String territoryFactionId
) {
}
