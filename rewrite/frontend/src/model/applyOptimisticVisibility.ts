import type { MapSnapshotDto } from "./transport";

export function applyOptimisticVisibility(
  snapshot: MapSnapshotDto | null,
  q: number,
  r: number,
  terrainHidden: boolean,
  role: "gm" | "player"
): MapSnapshotDto | null {
  if (!snapshot) {
    return snapshot;
  }

  if (role === "player" && terrainHidden) {
    return {
      ...snapshot,
      cells: snapshot.cells.filter((cell) => !(cell.q === q && cell.r === r))
    };
  }

  return {
    ...snapshot,
    cells: snapshot.cells.map((cell) =>
      cell.q === q && cell.r === r ? { ...cell, terrainHidden } : cell
    )
  };
}