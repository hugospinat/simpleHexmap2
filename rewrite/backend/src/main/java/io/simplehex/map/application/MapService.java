package io.simplehex.map.application;

import io.simplehex.map.domain.ActorRole;
import io.simplehex.map.domain.HexCoord;
import io.simplehex.map.persistence.JpaMapRepository;
import io.simplehex.map.persistence.MapPersistenceRecord;
import io.simplehex.map.transport.AppliedMapCommandDto;
import io.simplehex.map.transport.CellRefDto;
import io.simplehex.map.transport.CellVisibilityCommandRequest;
import io.simplehex.map.transport.CommandAppliedResponse;
import io.simplehex.map.transport.MapCommandRequest;
import io.simplehex.map.transport.MapSnapshotResponse;
import io.simplehex.map.transport.TerrainCommandRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MapService {

    private final ApplicationEventPublisher eventPublisher;
    private final JpaMapRepository mapRepository;

    public MapService(ApplicationEventPublisher eventPublisher, JpaMapRepository mapRepository) {
        this.eventPublisher = eventPublisher;
        this.mapRepository = mapRepository;
    }

    public MapSnapshotResponse getSnapshot(String mapId, ActorRole role) {
        MapPersistenceRecord map = requireMap(mapId);
        return new MapSnapshotResponse(map.mapId(), map.revision(), role, mapRepository.findCells(mapId, role));
    }

    @Transactional
    public CommandAppliedResponse applyCommand(String mapId, MapCommandRequest request) {
        if (!request.actorRole().canEditMapContent()) {
            throw new MapCommandException(HttpStatus.FORBIDDEN, "terrain_edit_forbidden");
        }

        requireMap(mapId);

        return mapRepository.findStoredCommand(mapId, request.operationId())
                .map(storedCommand -> new CommandAppliedResponse(
                        "command_applied",
                        storedCommand.operationId(),
                        mapId,
                        storedCommand.sequence(),
                        new AppliedMapCommandDto(
                                storedCommand.commandType(),
                                new CellRefDto(storedCommand.cellQ(), storedCommand.cellR()),
                                storedCommand.terrain(),
                                storedCommand.terrainHidden())))
                .orElseGet(() -> applyNewCommand(mapId, request));
    }

    @Transactional
    public void resetForTests() {
        mapRepository.resetSeedData();
    }

    private CommandAppliedResponse applyNewCommand(String mapId, MapCommandRequest request) {
        if (request instanceof TerrainCommandRequest terrainCommandRequest) {
            return applyTerrainCommand(mapId, terrainCommandRequest);
        }
        if (request instanceof CellVisibilityCommandRequest visibilityCommandRequest) {
            return applyVisibilityCommand(mapId, visibilityCommandRequest);
        }

        throw new MapCommandException(HttpStatus.BAD_REQUEST, "unsupported_command_type");
    }

    private CommandAppliedResponse applyTerrainCommand(String mapId, TerrainCommandRequest request) {
        if (!"set_cell_terrain".equals(request.type())) {
            throw new MapCommandException(HttpStatus.BAD_REQUEST, "unsupported_command_type");
        }

        HexCoord coord = new HexCoord(request.cell().q(), request.cell().r());
        if (!mapRepository.cellExists(mapId, coord)) {
            throw new MapCommandException(HttpStatus.NOT_FOUND, "cell_not_found");
        }

        mapRepository.updateCellTerrain(mapId, coord, request.terrain());
        long nextRevision = mapRepository.incrementRevision(mapId);
        mapRepository.insertTerrainCommandLog(mapId, request, nextRevision);

        CommandAppliedResponse response = new CommandAppliedResponse(
                "command_applied",
                request.operationId(),
                mapId,
                nextRevision,
                new AppliedMapCommandDto(
                        request.type(),
                        request.cell(),
                        request.terrain(),
                        null));
        eventPublisher.publishEvent(new MapCommandAppliedEvent(mapId, response));
        return response;
    }

    private CommandAppliedResponse applyVisibilityCommand(String mapId, CellVisibilityCommandRequest request) {
        if (!"set_cell_visibility".equals(request.type())) {
            throw new MapCommandException(HttpStatus.BAD_REQUEST, "unsupported_command_type");
        }

        HexCoord coord = new HexCoord(request.cell().q(), request.cell().r());
        if (!mapRepository.cellExists(mapId, coord)) {
            throw new MapCommandException(HttpStatus.NOT_FOUND, "cell_not_found");
        }

        mapRepository.updateCellTerrainHidden(mapId, coord, request.terrainHidden());
        long nextRevision = mapRepository.incrementRevision(mapId);
        mapRepository.insertVisibilityCommandLog(mapId, request, nextRevision);

        CommandAppliedResponse response = new CommandAppliedResponse(
                "command_applied",
                request.operationId(),
                mapId,
                nextRevision,
                new AppliedMapCommandDto(
                        request.type(),
                        request.cell(),
                        null,
                        request.terrainHidden()));
        eventPublisher.publishEvent(new MapCommandAppliedEvent(mapId, response));
        return response;
    }

    private MapPersistenceRecord requireMap(String mapId) {
        return mapRepository.findMap(mapId).orElseThrow(() -> new MapCommandException(HttpStatus.NOT_FOUND, "map_not_found"));
    }
}