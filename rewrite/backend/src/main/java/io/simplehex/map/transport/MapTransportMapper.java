package io.simplehex.map.transport;

import io.simplehex.map.application.AppliedMapCommand;
import io.simplehex.map.application.MapCellView;
import io.simplehex.map.application.MapCommandResult;
import io.simplehex.map.application.MapFactionView;
import io.simplehex.map.application.MapSnapshot;

public final class MapTransportMapper {

    private MapTransportMapper() {
    }

    public static MapSnapshotResponse toResponse(MapSnapshot snapshot) {
        return new MapSnapshotResponse(
                snapshot.mapId(),
                snapshot.revision(),
                snapshot.role(),
                snapshot.factions().stream().map(MapTransportMapper::toResponse).toList(),
                snapshot.cells().stream().map(MapTransportMapper::toResponse).toList());
    }

    public static CommandAppliedResponse toResponse(MapCommandResult result) {
        return new CommandAppliedResponse(
                result.type(),
                result.operationId(),
                result.mapId(),
                result.sequence(),
                toResponse(result.command()));
    }

    private static FactionResponse toResponse(MapFactionView faction) {
        return new FactionResponse(faction.id(), faction.label(), faction.color());
    }

    private static CellSnapshotResponse toResponse(MapCellView cell) {
        return new CellSnapshotResponse(
                cell.q(),
                cell.r(),
                cell.terrain(),
                cell.terrainHidden(),
                cell.featureHidden(),
                cell.territoryFactionId());
    }

    private static AppliedMapCommandResponse toResponse(AppliedMapCommand command) {
        return new AppliedMapCommandResponse(
                command.type(),
                command.cell(),
                command.terrain(),
                command.terrainHidden(),
                command.featureHidden(),
                command.territoryFactionId());
    }
}
