package io.simplehex.map.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "maps")
public class MapEntity {

    @Id
    @Column(name = "map_id", nullable = false, length = 100)
    private String mapId;

    @Column(name = "revision", nullable = false)
    private long revision;

    protected MapEntity() {
    }

    public MapEntity(String mapId, long revision) {
        this.mapId = mapId;
        this.revision = revision;
    }

    public String getMapId() {
        return mapId;
    }

    public long getRevision() {
        return revision;
    }

    public void setRevision(long revision) {
        this.revision = revision;
    }
}