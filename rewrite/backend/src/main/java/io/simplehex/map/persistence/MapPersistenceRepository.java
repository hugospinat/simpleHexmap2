package io.simplehex.map.persistence;

import io.simplehex.map.application.MapCommandException;
import io.simplehex.map.domain.ActorRole;
import io.simplehex.map.domain.HexCoord;
import io.simplehex.map.domain.TerrainType;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MapPersistenceRepository {

    private final EntityManager entityManager;

    public MapPersistenceRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<MapPersistenceRecord> findMap(String mapId) {
        MapEntity entity = entityManager.find(MapEntity.class, mapId);
        if (entity == null) {
            return Optional.empty();
        }

        return Optional.of(new MapPersistenceRecord(entity.getMapId(), entity.getRevision()));
    }

    public List<MapCellRecord> findCells(String mapId, ActorRole role) {
        String jpql = role == ActorRole.PLAYER
                ? """
                        select cell
                        from MapCellEntity cell
                        where cell.id.mapId = :mapId and cell.terrainHidden = false
                        order by cell.id.q, cell.id.r
                        """
                : """
                        select cell
                        from MapCellEntity cell
                        where cell.id.mapId = :mapId
                        order by cell.id.q, cell.id.r
                        """;

        return entityManager.createQuery(jpql, MapCellEntity.class)
                .setParameter("mapId", mapId)
                .getResultList()
                .stream()
                .map(cell -> new MapCellRecord(
                        cell.getId().getQ(),
                        cell.getId().getR(),
                        TerrainType.fromValue(cell.getTerrain()),
                        cell.isTerrainHidden(),
                        cell.isFeatureHidden(),
                        cell.getTerritoryFactionId()))
                .toList();
    }

    public boolean cellExists(String mapId, HexCoord coord) {
        return entityManager.find(MapCellEntity.class, new MapCellId(mapId, coord.q(), coord.r())) != null;
    }

    public Optional<StoredCommandRecord> findStoredCommand(String mapId, String operationId) {
        MapOperationLogEntity entity = entityManager.find(
                MapOperationLogEntity.class,
                new MapOperationLogId(mapId, operationId));
        if (entity == null) {
            return Optional.empty();
        }

        return Optional.of(new StoredCommandRecord(
            entity.getId().getOperationId(),
            entity.getSequence(),
            entity.getCommandType(),
            entity.getCellQ(),
            entity.getCellR(),
            entity.getTerrain() == null ? null : TerrainType.fromValue(entity.getTerrain()),
            entity.getTerrainHiddenValue(),
            entity.getFeatureHiddenValue(),
            entity.getTerritoryFactionIdValue()));
    }

    public long incrementRevision(String mapId) {
        MapEntity entity = entityManager.find(MapEntity.class, mapId);
        if (entity == null) {
            throw new MapCommandException(org.springframework.http.HttpStatus.NOT_FOUND, "map_not_found");
        }

        entity.setRevision(entity.getRevision() + 1);
        return entity.getRevision();
    }

    public void updateCellTerrain(String mapId, HexCoord coord, TerrainType terrain) {
        MapCellEntity entity = entityManager.find(MapCellEntity.class, new MapCellId(mapId, coord.q(), coord.r()));
        if (entity == null) {
            throw new MapCommandException(org.springframework.http.HttpStatus.NOT_FOUND, "cell_not_found");
        }

        entity.setTerrain(terrain.value());
    }

    public void updateCellTerrainHidden(String mapId, HexCoord coord, boolean terrainHidden) {
        MapCellEntity entity = entityManager.find(MapCellEntity.class, new MapCellId(mapId, coord.q(), coord.r()));
        if (entity == null) {
            throw new MapCommandException(org.springframework.http.HttpStatus.NOT_FOUND, "cell_not_found");
        }

        entity.setTerrainHidden(terrainHidden);
    }

    public void updateCellFeatureHidden(String mapId, HexCoord coord, boolean featureHidden) {
        MapCellEntity entity = entityManager.find(MapCellEntity.class, new MapCellId(mapId, coord.q(), coord.r()));
        if (entity == null) {
            throw new MapCommandException(org.springframework.http.HttpStatus.NOT_FOUND, "cell_not_found");
        }

        entity.setFeatureHidden(featureHidden);
    }

    public void updateCellTerritoryFaction(String mapId, HexCoord coord, String territoryFactionId) {
        MapCellEntity entity = entityManager.find(MapCellEntity.class, new MapCellId(mapId, coord.q(), coord.r()));
        if (entity == null) {
            throw new MapCommandException(org.springframework.http.HttpStatus.NOT_FOUND, "cell_not_found");
        }

        entity.setTerritoryFactionId(territoryFactionId);
    }

    public void insertTerrainCommandLog(
            String mapId,
            String operationId,
            String commandType,
            ActorRole actorRole,
            HexCoord cell,
            TerrainType terrain,
            long sequence
    ) {
        entityManager.persist(new MapOperationLogEntity(
                new MapOperationLogId(mapId, operationId),
                sequence,
                commandType,
                actorRole.value(),
                cell.q(),
                cell.r(),
                terrain.value(),
                null,
                null,
                null));
    }

    public void insertVisibilityCommandLog(
            String mapId,
            String operationId,
            String commandType,
            ActorRole actorRole,
            HexCoord cell,
            boolean terrainHidden,
            long sequence
    ) {
        entityManager.persist(new MapOperationLogEntity(
                new MapOperationLogId(mapId, operationId),
                sequence,
                commandType,
                actorRole.value(),
                cell.q(),
                cell.r(),
                null,
                terrainHidden,
                null,
                null));
    }

    public void insertFeatureVisibilityCommandLog(
            String mapId,
            String operationId,
            String commandType,
            ActorRole actorRole,
            HexCoord cell,
            boolean featureHidden,
            long sequence
    ) {
        entityManager.persist(new MapOperationLogEntity(
                new MapOperationLogId(mapId, operationId),
                sequence,
                commandType,
                actorRole.value(),
                cell.q(),
                cell.r(),
                null,
                null,
                featureHidden,
                null));
    }

    public void insertTerritoryCommandLog(
            String mapId,
            String operationId,
            String commandType,
            ActorRole actorRole,
            HexCoord cell,
            String territoryFactionId,
            long sequence
    ) {
        entityManager.persist(new MapOperationLogEntity(
                new MapOperationLogId(mapId, operationId),
                sequence,
                commandType,
                actorRole.value(),
                cell.q(),
                cell.r(),
                null,
                null,
                null,
                territoryFactionId));
    }

    public void resetSeedData() {
        entityManager.createQuery("delete from MapOperationLogEntity").executeUpdate();
        entityManager.createQuery("delete from MapCellEntity").executeUpdate();
        entityManager.createQuery("delete from MapEntity").executeUpdate();

        persistDemoMapSeed();
    }

    @Transactional
    public void seedDemoMapIfMissing() {
        if (entityManager.find(MapEntity.class, "demo-map") != null) {
            return;
        }

        persistDemoMapSeed();
    }

    private void persistDemoMapSeed() {
        entityManager.persist(new MapEntity("demo-map", 0));
        entityManager.persist(new MapCellEntity(new MapCellId("demo-map", 0, 0), "plains", false, false, null));
        entityManager.persist(new MapCellEntity(new MapCellId("demo-map", 1, 0), "forest", false, false, "amber"));
        entityManager.persist(new MapCellEntity(new MapCellId("demo-map", 2, 0), "hills", false, false, "violet"));
    }
}
