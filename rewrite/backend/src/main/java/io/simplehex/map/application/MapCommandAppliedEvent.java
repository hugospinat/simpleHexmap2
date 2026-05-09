package io.simplehex.map.application;

public record MapCommandAppliedEvent(String mapId, MapCommandResult result) {
}
