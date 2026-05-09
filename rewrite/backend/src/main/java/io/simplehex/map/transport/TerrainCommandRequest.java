package io.simplehex.map.transport;

import io.simplehex.map.application.SetCellTerrainCommand;
import io.simplehex.map.domain.HexCoord;
import io.simplehex.map.domain.TerrainType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TerrainCommandRequest(
        @NotBlank String type,
        @NotBlank String operationId,
        @NotNull @Valid HexCoord cell,
        @NotNull TerrainType terrain
) implements MapCommandRequest, SetCellTerrainCommand {
}
