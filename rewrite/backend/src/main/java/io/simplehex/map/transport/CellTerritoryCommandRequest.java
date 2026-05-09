package io.simplehex.map.transport;

import io.simplehex.map.application.SetCellTerritoryCommand;
import io.simplehex.map.domain.HexCoord;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CellTerritoryCommandRequest(
        @NotBlank String type,
        @NotBlank String operationId,
        @NotNull @Valid HexCoord cell,
        String territoryFactionId
) implements MapCommandRequest, SetCellTerritoryCommand {
}
