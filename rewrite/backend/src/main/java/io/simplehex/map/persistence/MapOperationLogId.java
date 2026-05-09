package io.simplehex.map.persistence;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class MapOperationLogId implements Serializable {

    @Column(name = "map_id", nullable = false, length = 100)
    private String mapId;

    @Column(name = "operation_id", nullable = false, length = 100)
    private String operationId;

    protected MapOperationLogId() {
    }

    public MapOperationLogId(String mapId, String operationId) {
        this.mapId = mapId;
        this.operationId = operationId;
    }

    public String getMapId() {
        return mapId;
    }

    public String getOperationId() {
        return operationId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MapOperationLogId that)) {
            return false;
        }
        return Objects.equals(mapId, that.mapId) && Objects.equals(operationId, that.operationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapId, operationId);
    }
}