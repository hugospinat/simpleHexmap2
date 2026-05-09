package io.simplehex.map.transport;

import io.simplehex.map.application.SetCellFeatureVisibilityCommand;
import io.simplehex.map.domain.HexCoord;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeatureVisibilityCommandRequest(
        @NotBlank String type,
        @NotBlank String operationId,
        @NotNull @Valid HexCoord cell,
        boolean featureHidden
) implements MapCommandRequest, SetCellFeatureVisibilityCommand {
}
