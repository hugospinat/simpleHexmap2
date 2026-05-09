package io.simplehex.map.transport;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeatureVisibilityCommandRequest(
        @NotBlank String type,
        @NotBlank String operationId,
        @NotNull @Valid CellRefDto cell,
        boolean featureHidden
) implements MapCommandRequest {
}
