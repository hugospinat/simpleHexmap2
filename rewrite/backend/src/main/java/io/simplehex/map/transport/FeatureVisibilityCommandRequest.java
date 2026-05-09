package io.simplehex.map.transport;

import io.simplehex.map.domain.ActorRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeatureVisibilityCommandRequest(
        @NotBlank String type,
        @NotBlank String operationId,
        @NotNull ActorRole actorRole,
        @NotNull @Valid CellRefDto cell,
        boolean featureHidden
) implements MapCommandRequest {
}
