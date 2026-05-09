package io.simplehex.map.application;

import io.simplehex.map.domain.ActorRole;
import java.util.List;

public record MapSnapshot(
        String mapId,
        long revision,
        ActorRole role,
        List<MapFactionView> factions,
        List<MapCellView> cells
) {
}
