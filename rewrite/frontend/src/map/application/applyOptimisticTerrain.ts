import type { MapSnapshotDto, TerrainType } from "../transport/dto";

export function applyOptimisticTerrain(
  snapshot: MapSnapshotDto | null,
  q: number,
  r: number,
  terrain: TerrainType
): MapSnapshotDto | null {
  if (!snapshot) {
    return snapshot;
  }

  return {
    ...snapshot,
    cells: snapshot.cells.map((cell) =>
      cell.q === q && cell.r === r ? { ...cell, terrain } : cell
    )
  };
}
