package io.simplehex.map.application;

import io.simplehex.map.domain.ActorRole;
import io.simplehex.map.domain.HexCoord;
import io.simplehex.map.persistence.JpaMapRepository;
import io.simplehex.map.persistence.MapPersistenceRecord;
import io.simplehex.map.transport.AppliedMapCommandDto;
import io.simplehex.map.transport.CellRefDto;
import io.simplehex.map.transport.CellTerritoryCommandRequest;
import io.simplehex.map.transport.CellVisibilityCommandRequest;
import io.simplehex.map.transport.CommandAppliedResponse;
import io.simplehex.map.transport.FactionDto;
import io.simplehex.map.transport.FeatureVisibilityCommandRequest;
import io.simplehex.map.transport.MapCommandRequest;
import io.simplehex.map.transport.MapSnapshotResponse;
import io.simplehex.map.transport.TerrainCommandRequest;
import io.simplehex.session.AuthenticatedActor;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MapService {

    private static final List<FactionDto> DEMO_FACTIONS = List.of(
            new FactionDto("amber", "Amber Wardens", "#d08a2f"),
            new FactionDto("violet", "Violet League", "#7b61ff"));

    private final ApplicationEventPublisher eventPublisher;
    private final JpaMapRepository mapRepository;

    public MapService(ApplicationEventPublisher eventPublisher, JpaMapRepository mapRepository) {
        this.eventPublisher = eventPublisher;
        this.mapRepository = mapRepository;
    }

    public MapSnapshotResponse getSnapshot(String mapId, AuthenticatedActor actor) {
        MapPersistenceRecord map = requireMap(mapId);
        if (!actor.isMemberOf(mapId)) {
            throw new MapCommandException(HttpStatus.FORBIDDEN, "map_access_forbidden");
        }
        return new MapSnapshotResponse(map.mapId(), map.revision(), actor.role(), DEMO_FACTIONS, mapRepository.findCells(mapId, actor.role()));
    }

    @Transactional
    public CommandAppliedResponse applyCommand(String mapId, MapCommandRequest request, AuthenticatedActor actor) {
        if (!actor.isMemberOf(mapId)) {
            throw new MapCommandException(HttpStatus.FORBIDDEN, "map_access_forbidden");
        }
        if (!actor.role().canEditMapContent()) {
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
                                storedCommand.terrainHidden(),
                                storedCommand.featureHidden(),
                                storedCommand.territoryFactionId())))
                .orElseGet(() -> applyNewCommand(mapId, request, actor.role()));
    }

    @Transactional
    public void resetForTests() {
        mapRepository.resetSeedData();
    }

    private CommandAppliedResponse applyNewCommand(String mapId, MapCommandRequest request, ActorRole actorRole) {
        if (request instanceof TerrainCommandRequest terrainCommandRequest) {
            return applyTerrainCommand(mapId, terrainCommandRequest, actorRole);
        }
        if (request instanceof CellVisibilityCommandRequest visibilityCommandRequest) {
            return applyVisibilityCommand(mapId, visibilityCommandRequest, actorRole);
        }
        if (request instanceof FeatureVisibilityCommandRequest featureVisibilityCommandRequest) {
            return applyFeatureVisibilityCommand(mapId, featureVisibilityCommandRequest, actorRole);
        }
        if (request instanceof CellTerritoryCommandRequest territoryCommandRequest) {
            return applyTerritoryCommand(mapId, territoryCommandRequest, actorRole);
        }

        throw new MapCommandException(HttpStatus.BAD_REQUEST, "unsupported_command_type");
    }

    private CommandAppliedResponse applyTerrainCommand(String mapId, TerrainCommandRequest request, ActorRole actorRole) {
        if (!"set_cell_terrain".equals(request.type())) {
            throw new MapCommandException(HttpStatus.BAD_REQUEST, "unsupported_command_type");
        }

        HexCoord coord = new HexCoord(request.cell().q(), request.cell().r());
        if (!mapRepository.cellExists(mapId, coord)) {
            throw new MapCommandException(HttpStatus.NOT_FOUND, "cell_not_found");
        }

        mapRepository.updateCellTerrain(mapId, coord, request.terrain());
        long nextRevision = mapRepository.incrementRevision(mapId);
        mapRepository.insertTerrainCommandLog(mapId, request, actorRole, nextRevision);

        CommandAppliedResponse response = new CommandAppliedResponse(
                "command_applied",
                request.operationId(),
                mapId,
                nextRevision,
                new AppliedMapCommandDto(
                        request.type(),
                        request.cell(),
                        request.terrain(),
                        null,
                        null,
                        null));
        eventPublisher.publishEvent(new MapCommandAppliedEvent(mapId, response));
        return response;
    }

    private CommandAppliedResponse applyVisibilityCommand(String mapId, CellVisibilityCommandRequest request, ActorRole actorRole) {
        if (!"set_cell_visibility".equals(request.type())) {
            throw new MapCommandException(HttpStatus.BAD_REQUEST, "unsupported_command_type");
        }

        HexCoord coord = new HexCoord(request.cell().q(), request.cell().r());
        if (!mapRepository.cellExists(mapId, coord)) {
            throw new MapCommandException(HttpStatus.NOT_FOUND, "cell_not_found");
        }

        mapRepository.updateCellTerrainHidden(mapId, coord, request.terrainHidden());
        long nextRevision = mapRepository.incrementRevision(mapId);
        mapRepository.insertVisibilityCommandLog(mapId, request, actorRole, nextRevision);

        CommandAppliedResponse response = new CommandAppliedResponse(
                "command_applied",
                request.operationId(),
                mapId,
                nextRevision,
                new AppliedMapCommandDto(
                        request.type(),
                        request.cell(),
                        null,
                        request.terrainHidden(),
                        null,
                        null));
        eventPublisher.publishEvent(new MapCommandAppliedEvent(mapId, response));
        return response;
    }

    private CommandAppliedResponse applyFeatureVisibilityCommand(String mapId, FeatureVisibilityCommandRequest request, ActorRole actorRole) {
        if (!"set_cell_feature_visibility".equals(request.type())) {
            throw new MapCommandException(HttpStatus.BAD_REQUEST, "unsupported_command_type");
        }

        HexCoord coord = new HexCoord(request.cell().q(), request.cell().r());
        if (!mapRepository.cellExists(mapId, coord)) {
            throw new MapCommandException(HttpStatus.NOT_FOUND, "cell_not_found");
        }

        mapRepository.updateCellFeatureHidden(mapId, coord, request.featureHidden());
        long nextRevision = mapRepository.incrementRevision(mapId);
        mapRepository.insertFeatureVisibilityCommandLog(mapId, request, actorRole, nextRevision);

        CommandAppliedResponse response = new CommandAppliedResponse(
                "command_applied",
                request.operationId(),
                mapId,
                nextRevision,
                new AppliedMapCommandDto(
                        request.type(),
                        request.cell(),
                        null,
                        null,
                        request.featureHidden(),
                        null));
        eventPublisher.publishEvent(new MapCommandAppliedEvent(mapId, response));
        return response;
    }

    private CommandAppliedResponse applyTerritoryCommand(String mapId, CellTerritoryCommandRequest request, ActorRole actorRole) {
        if (!"set_cell_territory".equals(request.type())) {
            throw new MapCommandException(HttpStatus.BAD_REQUEST, "unsupported_command_type");
        }

        if (request.territoryFactionId() != null && DEMO_FACTIONS.stream().noneMatch(faction -> faction.id().equals(request.territoryFactionId()))) {
            throw new MapCommandException(HttpStatus.BAD_REQUEST, "unknown_faction_id");
        }

        HexCoord coord = new HexCoord(request.cell().q(), request.cell().r());
        if (!mapRepository.cellExists(mapId, coord)) {
            throw new MapCommandException(HttpStatus.NOT_FOUND, "cell_not_found");
        }

        mapRepository.updateCellTerritoryFaction(mapId, coord, request.territoryFactionId());
        long nextRevision = mapRepository.incrementRevision(mapId);
        mapRepository.insertTerritoryCommandLog(mapId, request, actorRole, nextRevision);

        CommandAppliedResponse response = new CommandAppliedResponse(
                "command_applied",
                request.operationId(),
                mapId,
                nextRevision,
                new AppliedMapCommandDto(
                        request.type(),
                        request.cell(),
                        null,
                        null,
                        null,
                        request.territoryFactionId()));
        eventPublisher.publishEvent(new MapCommandAppliedEvent(mapId, response));
        return response;
    }

    private MapPersistenceRecord requireMap(String mapId) {
        return mapRepository.findMap(mapId).orElseThrow(() -> new MapCommandException(HttpStatus.NOT_FOUND, "map_not_found"));
    }
}
