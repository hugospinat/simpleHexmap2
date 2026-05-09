package io.simplehex.map.persistence;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class MapCellId implements Serializable {

    @Column(name = "map_id", nullable = false, length = 100)
    private String mapId;

    @Column(name = "q", nullable = false)
    private int q;

    @Column(name = "r", nullable = false)
    private int r;

    protected MapCellId() {
    }

    public MapCellId(String mapId, int q, int r) {
        this.mapId = mapId;
        this.q = q;
        this.r = r;
    }

    public String getMapId() {
        return mapId;
    }

    public int getQ() {
        return q;
    }

    public int getR() {
        return r;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MapCellId that)) {
            return false;
        }
        return q == that.q && r == that.r && Objects.equals(mapId, that.mapId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapId, q, r);
    }
}