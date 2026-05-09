import type { MapSnapshotDto } from "../transport/dto";

export function applyOptimisticTerritory(
  snapshot: MapSnapshotDto | null,
  q: number,
  r: number,
  territoryFactionId: string | null
): MapSnapshotDto | null {
  if (!snapshot) {
    return snapshot;
  }

  return {
    ...snapshot,
    cells: snapshot.cells.map((cell) =>
      cell.q === q && cell.r === r ? { ...cell, territoryFactionId } : cell
    )
  };
}
