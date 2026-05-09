package io.simplehex.map.application;

import io.simplehex.map.domain.TerrainType;

public interface SetCellTerrainCommand extends MapCommand {
    TerrainType terrain();
}
