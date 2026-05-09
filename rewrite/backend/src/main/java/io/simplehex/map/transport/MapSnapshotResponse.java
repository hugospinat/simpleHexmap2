package io.simplehex.map.transport;

import io.simplehex.map.domain.ActorRole;
import java.util.List;

public record MapSnapshotResponse(
        String mapId,
        long revision,
        ActorRole role,
        List<FactionResponse> factions,
        List<CellSnapshotResponse> cells
) {
}
