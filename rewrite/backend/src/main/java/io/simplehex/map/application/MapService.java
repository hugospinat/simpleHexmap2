package io.simplehex.map.application;

import io.simplehex.map.domain.ActorRole;
import io.simplehex.map.domain.HexCoord;
import io.simplehex.map.persistence.MapCellRecord;
import io.simplehex.map.persistence.MapPersistenceRepository;
import io.simplehex.map.persistence.MapPersistenceRecord;
import io.simplehex.session.AuthenticatedActor;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MapService {

    private static final List<MapFactionView> DEMO_FACTIONS = List.of(
            new MapFactionView("amber", "Amber Wardens", "#d08a2f"),
            new MapFactionView("violet", "Violet League", "#7b61ff"));

    private final ApplicationEventPublisher eventPublisher;
    private final MapPersistenceRepository mapRepository;

    public MapService(ApplicationEventPublisher eventPublisher, MapPersistenceRepository mapRepository) {
        this.eventPublisher = eventPublisher;
        this.mapRepository = mapRepository;
    }

    public MapSnapshot getSnapshot(String mapId, AuthenticatedActor actor) {
        MapPersistenceRecord map = requireMap(mapId);
        if (!actor.isMemberOf(mapId)) {
            throw new MapCommandException(HttpStatus.FORBIDDEN, "map_access_forbidden");
        }
        return new MapSnapshot(
                map.mapId(),
                map.revision(),
                actor.role(),
                DEMO_FACTIONS,
                mapRepository.findCells(mapId, actor.role()).stream().map(this::toMapCellView).toList());
    }

    @Transactional
    public MapCommandResult applyCommand(String mapId, MapCommand request, AuthenticatedActor actor) {
        if (!actor.isMemberOf(mapId)) {
            throw new MapCommandException(HttpStatus.FORBIDDEN, "map_access_forbidden");
        }
        if (!actor.role().canEditMapContent()) {
            throw new MapCommandException(HttpStatus.FORBIDDEN, "terrain_edit_forbidden");
        }

        requireMap(mapId);

        return mapRepository.findStoredCommand(mapId, request.operationId())
                .map(storedCommand -> new MapCommandResult(
                        "command_applied",
                        storedCommand.operationId(),
                        mapId,
                        storedCommand.sequence(),
                        new AppliedMapCommand(
                                storedCommand.commandType(),
                                new HexCoord(storedCommand.cellQ(), storedCommand.cellR()),
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

    private MapCommandResult applyNewCommand(String mapId, MapCommand request, ActorRole actorRole) {
        if (request instanceof SetCellTerrainCommand terrainCommandRequest) {
            return applyTerrainCommand(mapId, terrainCommandRequest, actorRole);
        }
        if (request instanceof SetCellVisibilityCommand visibilityCommandRequest) {
            return applyVisibilityCommand(mapId, visibilityCommandRequest, actorRole);
        }
        if (request instanceof SetCellFeatureVisibilityCommand featureVisibilityCommandRequest) {
            return applyFeatureVisibilityCommand(mapId, featureVisibilityCommandRequest, actorRole);
        }
        if (request instanceof SetCellTerritoryCommand territoryCommandRequest) {
            return applyTerritoryCommand(mapId, territoryCommandRequest, actorRole);
        }

        throw new MapCommandException(HttpStatus.BAD_REQUEST, "unsupported_command_type");
    }

    private MapCommandResult applyTerrainCommand(String mapId, SetCellTerrainCommand request, ActorRole actorRole) {
        if (!"set_cell_terrain".equals(request.type())) {
            throw new MapCommandException(HttpStatus.BAD_REQUEST, "unsupported_command_type");
        }

        HexCoord coord = request.cell();
        if (!mapRepository.cellExists(mapId, coord)) {
            throw new MapCommandException(HttpStatus.NOT_FOUND, "cell_not_found");
        }

        mapRepository.updateCellTerrain(mapId, coord, request.terrain());
        long nextRevision = mapRepository.incrementRevision(mapId);
        mapRepository.insertTerrainCommandLog(mapId, request.operationId(), request.type(), actorRole, coord, request.terrain(), nextRevision);

        MapCommandResult response = new MapCommandResult(
                "command_applied",
                request.operationId(),
                mapId,
                nextRevision,
                new AppliedMapCommand(
                        request.type(),
                        request.cell(),
                        request.terrain(),
                        null,
                        null,
                        null));
        eventPublisher.publishEvent(new MapCommandAppliedEvent(mapId, response));
        return response;
    }

    private MapCommandResult applyVisibilityCommand(String mapId, SetCellVisibilityCommand request, ActorRole actorRole) {
        if (!"set_cell_visibility".equals(request.type())) {
            throw new MapCommandException(HttpStatus.BAD_REQUEST, "unsupported_command_type");
        }

        HexCoord coord = request.cell();
        if (!mapRepository.cellExists(mapId, coord)) {
            throw new MapCommandException(HttpStatus.NOT_FOUND, "cell_not_found");
        }

        mapRepository.updateCellTerrainHidden(mapId, coord, request.terrainHidden());
        long nextRevision = mapRepository.incrementRevision(mapId);
        mapRepository.insertVisibilityCommandLog(mapId, request.operationId(), request.type(), actorRole, coord, request.terrainHidden(), nextRevision);

        MapCommandResult response = new MapCommandResult(
                "command_applied",
                request.operationId(),
                mapId,
                nextRevision,
                new AppliedMapCommand(
                        request.type(),
                        request.cell(),
                        null,
                        request.terrainHidden(),
                        null,
                        null));
        eventPublisher.publishEvent(new MapCommandAppliedEvent(mapId, response));
        return response;
    }

    private MapCommandResult applyFeatureVisibilityCommand(String mapId, SetCellFeatureVisibilityCommand request, ActorRole actorRole) {
        if (!"set_cell_feature_visibility".equals(request.type())) {
            throw new MapCommandException(HttpStatus.BAD_REQUEST, "unsupported_command_type");
        }

        HexCoord coord = request.cell();
        if (!mapRepository.cellExists(mapId, coord)) {
            throw new MapCommandException(HttpStatus.NOT_FOUND, "cell_not_found");
        }

        mapRepository.updateCellFeatureHidden(mapId, coord, request.featureHidden());
        long nextRevision = mapRepository.incrementRevision(mapId);
        mapRepository.insertFeatureVisibilityCommandLog(mapId, request.operationId(), request.type(), actorRole, coord, request.featureHidden(), nextRevision);

        MapCommandResult response = new MapCommandResult(
                "command_applied",
                request.operationId(),
                mapId,
                nextRevision,
                new AppliedMapCommand(
                        request.type(),
                        request.cell(),
                        null,
                        null,
                        request.featureHidden(),
                        null));
        eventPublisher.publishEvent(new MapCommandAppliedEvent(mapId, response));
        return response;
    }

    private MapCommandResult applyTerritoryCommand(String mapId, SetCellTerritoryCommand request, ActorRole actorRole) {
        if (!"set_cell_territory".equals(request.type())) {
            throw new MapCommandException(HttpStatus.BAD_REQUEST, "unsupported_command_type");
        }

        if (request.territoryFactionId() != null && DEMO_FACTIONS.stream().noneMatch(faction -> faction.id().equals(request.territoryFactionId()))) {
            throw new MapCommandException(HttpStatus.BAD_REQUEST, "unknown_faction_id");
        }

        HexCoord coord = request.cell();
        if (!mapRepository.cellExists(mapId, coord)) {
            throw new MapCommandException(HttpStatus.NOT_FOUND, "cell_not_found");
        }

        mapRepository.updateCellTerritoryFaction(mapId, coord, request.territoryFactionId());
        long nextRevision = mapRepository.incrementRevision(mapId);
        mapRepository.insertTerritoryCommandLog(mapId, request.operationId(), request.type(), actorRole, coord, request.territoryFactionId(), nextRevision);

        MapCommandResult response = new MapCommandResult(
                "command_applied",
                request.operationId(),
                mapId,
                nextRevision,
                new AppliedMapCommand(
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

    private MapCellView toMapCellView(MapCellRecord cell) {
        return new MapCellView(
                cell.q(),
                cell.r(),
                cell.terrain(),
                cell.terrainHidden(),
                cell.featureHidden(),
                cell.territoryFactionId());
    }
}
