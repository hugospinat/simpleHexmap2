import type { ActorRole, MapSnapshotDto } from "../transport/dto";

export function applyOptimisticVisibility(
  snapshot: MapSnapshotDto | null,
  q: number,
  r: number,
  terrainHidden: boolean,
  role: ActorRole
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

export function applyOptimisticFeatureVisibility(
  snapshot: MapSnapshotDto | null,
  q: number,
  r: number,
  featureHidden: boolean
): MapSnapshotDto | null {
  if (!snapshot) {
    return snapshot;
  }

  return {
    ...snapshot,
    cells: snapshot.cells.map((cell) =>
      cell.q === q && cell.r === r ? { ...cell, featureHidden } : cell
    )
  };
}
