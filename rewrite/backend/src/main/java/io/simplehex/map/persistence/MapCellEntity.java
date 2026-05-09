package io.simplehex.map.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "map_cells")
public class MapCellEntity {

    @EmbeddedId
    private MapCellId id;

    @Column(name = "terrain", nullable = false, length = 32)
    private String terrain;

    @Column(name = "terrain_hidden", nullable = false)
    private boolean terrainHidden;

    @Column(name = "feature_hidden", nullable = false)
    private boolean featureHidden;

    @Column(name = "territory_faction_id", length = 64)
    private String territoryFactionId;

    protected MapCellEntity() {
    }

    public MapCellEntity(MapCellId id, String terrain, boolean terrainHidden, boolean featureHidden, String territoryFactionId) {
        this.id = id;
        this.terrain = terrain;
        this.terrainHidden = terrainHidden;
        this.featureHidden = featureHidden;
        this.territoryFactionId = territoryFactionId;
    }

    public MapCellId getId() {
        return id;
    }

    public String getTerrain() {
        return terrain;
    }

    public void setTerrain(String terrain) {
        this.terrain = terrain;
    }

    public boolean isTerrainHidden() {
        return terrainHidden;
    }

    public void setTerrainHidden(boolean terrainHidden) {
        this.terrainHidden = terrainHidden;
    }

    public boolean isFeatureHidden() {
        return featureHidden;
    }

    public void setFeatureHidden(boolean featureHidden) {
        this.featureHidden = featureHidden;
    }

    public String getTerritoryFactionId() {
        return territoryFactionId;
    }

    public void setTerritoryFactionId(String territoryFactionId) {
        this.territoryFactionId = territoryFactionId;
    }
}
