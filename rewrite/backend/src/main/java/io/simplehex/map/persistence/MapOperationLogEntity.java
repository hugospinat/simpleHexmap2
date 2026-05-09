package io.simplehex.map.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "map_operation_log",
    uniqueConstraints = @UniqueConstraint(name = "uq_map_sequence", columnNames = { "map_id", "sequence" })
)
public class MapOperationLogEntity {

    @EmbeddedId
    private MapOperationLogId id;

    @Column(name = "sequence", nullable = false)
    private long sequence;

    @Column(name = "command_type", nullable = false, length = 64)
    private String commandType;

    @Column(name = "actor_role", nullable = false, length = 32)
    private String actorRole;

    @Column(name = "cell_q", nullable = false)
    private int cellQ;

    @Column(name = "cell_r", nullable = false)
    private int cellR;

    @Column(name = "terrain", length = 32)
    private String terrain;

    @Column(name = "terrain_hidden_value")
    private Boolean terrainHiddenValue;

    @Column(name = "feature_hidden_value")
    private Boolean featureHiddenValue;

    protected MapOperationLogEntity() {
    }

    public MapOperationLogEntity(
            MapOperationLogId id,
            long sequence,
            String commandType,
            String actorRole,
            int cellQ,
            int cellR,
            String terrain,
            Boolean terrainHiddenValue,
            Boolean featureHiddenValue
    ) {
        this.id = id;
        this.sequence = sequence;
        this.commandType = commandType;
        this.actorRole = actorRole;
        this.cellQ = cellQ;
        this.cellR = cellR;
        this.terrain = terrain;
        this.terrainHiddenValue = terrainHiddenValue;
        this.featureHiddenValue = featureHiddenValue;
    }

    public MapOperationLogId getId() {
        return id;
    }

    public long getSequence() {
        return sequence;
    }

    public String getCommandType() {
        return commandType;
    }

    public int getCellQ() {
        return cellQ;
    }

    public int getCellR() {
        return cellR;
    }

    public String getTerrain() {
        return terrain;
    }

    public Boolean getTerrainHiddenValue() {
        return terrainHiddenValue;
    }

    public Boolean getFeatureHiddenValue() {
        return featureHiddenValue;
    }
}
