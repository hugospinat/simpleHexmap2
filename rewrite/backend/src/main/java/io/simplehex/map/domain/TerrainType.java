package io.simplehex.map.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TerrainType {
    PLAINS("plains"),
    FOREST("forest"),
    HILLS("hills"),
    WATER("water");

    private final String value;

    TerrainType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static TerrainType fromValue(String value) {
        for (TerrainType terrainType : values()) {
            if (terrainType.value.equalsIgnoreCase(value)) {
                return terrainType;
            }
        }

        throw new IllegalArgumentException("Unsupported terrain type: " + value);
    }
}