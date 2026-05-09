package io.simplehex.map.application;

public record MapCommandResult(
        String type,
        String operationId,
        String mapId,
        long sequence,
        AppliedMapCommand command
) {
}
