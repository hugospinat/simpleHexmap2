import type {
  CommandAppliedResponseDto,
  MapSnapshotDto,
  RealtimeMessageDto
} from "../transport/dto";

function applyCommand(snapshot: MapSnapshotDto, message: CommandAppliedResponseDto): MapSnapshotDto {
  if (message.command.type === "set_cell_terrain" && message.command.terrain !== null) {
    const { cell: targetCell, terrain } = message.command;

    return {
      ...snapshot,
      revision: message.sequence,
      cells: snapshot.cells.map((cell) =>
        cell.q === targetCell.q && cell.r === targetCell.r
          ? { ...cell, terrain }
          : cell
      )
    };
  }

  if (message.command.type === "set_cell_visibility" && message.command.terrainHidden !== null) {
    const { cell: targetCell, terrainHidden } = message.command;

    return {
      ...snapshot,
      revision: message.sequence,
      cells: snapshot.cells.map((cell) =>
        cell.q === targetCell.q && cell.r === targetCell.r
          ? { ...cell, terrainHidden }
          : cell
      )
    };
  }

  const { cell: targetCell, featureHidden } = message.command;

  return {
    ...snapshot,
    revision: message.sequence,
    cells: snapshot.cells.map((cell) =>
      cell.q === targetCell.q && cell.r === targetCell.r
        ? { ...cell, featureHidden }
        : cell
    )
  };
}

export function applyRealtimeMessage(
  currentSnapshot: MapSnapshotDto | null,
  message: RealtimeMessageDto
): MapSnapshotDto | null {
  if (message.type === "sync_snapshot") {
    return message.snapshot;
  }

  if (!currentSnapshot) {
    return currentSnapshot;
  }

  return applyCommand(currentSnapshot, message);
}
