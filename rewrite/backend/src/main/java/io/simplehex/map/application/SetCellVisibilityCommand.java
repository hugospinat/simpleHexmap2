package io.simplehex.map.application;

public interface SetCellVisibilityCommand extends MapCommand {
    boolean terrainHidden();
}
