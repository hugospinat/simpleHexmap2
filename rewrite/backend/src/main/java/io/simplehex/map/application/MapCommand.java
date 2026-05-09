package io.simplehex.map.application;

import io.simplehex.map.domain.HexCoord;

public interface MapCommand {
    String type();

    String operationId();

    HexCoord cell();
}
