package io.simplehex.map.transport;

public record CommandAppliedResponse(
        String type,
        String operationId,
        String mapId,
        long sequence,
        AppliedMapCommandDto command
) {
}