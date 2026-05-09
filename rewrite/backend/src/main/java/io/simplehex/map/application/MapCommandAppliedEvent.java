package io.simplehex.map.application;

import io.simplehex.map.transport.CommandAppliedResponse;

public record MapCommandAppliedEvent(String mapId, CommandAppliedResponse response) {
}