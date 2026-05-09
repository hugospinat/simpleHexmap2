import type { RenderModel } from "./renderModel";
import type { MapSnapshotDto, TerrainType } from "./transport";

const terrainPalette: Record<TerrainType, number> = {
  forest: 0x3b6b4d,
  hills: 0x86633d,
  water: 0x4470aa,
  plains: 0xa28752
};

export function projectRenderModel(snapshot: MapSnapshotDto): RenderModel {
  return {
    title: `Terrain slice preview · rev ${snapshot.revision}`,
    subtitle: `Render model projected from a ${snapshot.role.toUpperCase()} snapshot DTO.`,
    terrainTiles: snapshot.cells.map((cell) => ({
      q: cell.q,
      r: cell.r,
      terrain: cell.terrain,
      fill: terrainPalette[cell.terrain],
      label: `${cell.terrain}${cell.terrainHidden ? " · hidden" : ""} (${cell.q}, ${cell.r})`,
      terrainHidden: cell.terrainHidden
    }))
  };
}