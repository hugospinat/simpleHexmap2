package io.simplehex.map.transport;

import jakarta.validation.constraints.NotNull;

public record CellRefDto(
        @NotNull Integer q,
        @NotNull Integer r
) {
}