package io.simplehex.map.persistence;

import io.simplehex.map.domain.TerrainType;

public record StoredCommandRecord(
        String operationId,
        long sequence,
        String commandType,
        int cellQ,
        int cellR,
        TerrainType terrain,
        Boolean terrainHidden,
        Boolean featureHidden,
        String territoryFactionId
) {
}
