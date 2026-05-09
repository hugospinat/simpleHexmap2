import type { RenderModel } from "./renderModel";
import type { MapSnapshotDto, TerrainType } from "../transport/dto";

const terrainPalette: Record<TerrainType, number> = {
  forest: 0x3b6b4d,
  hills: 0x86633d,
  water: 0x4470aa,
  plains: 0xa28752
};

export function projectRenderModel(snapshot: MapSnapshotDto): RenderModel {
  const factionById = new Map(snapshot.factions.map((faction) => [faction.id, faction]));

  return {
    title: `Map slice preview · rev ${snapshot.revision}`,
    subtitle: `Render model projected from a ${snapshot.role.toUpperCase()} snapshot DTO.`,
    terrainTiles: snapshot.cells.map((cell) => ({
      q: cell.q,
      r: cell.r,
      terrain: cell.terrain,
      fill: terrainPalette[cell.terrain],
      label: `${cell.terrain} (${cell.q}, ${cell.r})`,
      detailLabel: [
        cell.territoryFactionId ? `${factionById.get(cell.territoryFactionId)?.label ?? cell.territoryFactionId} territory` : null,
        cell.terrainHidden ? "terrain hidden" : null,
        cell.featureHidden ? "feature hidden" : null
      ]
        .filter(Boolean)
        .join(" · "),
      terrainHidden: cell.terrainHidden,
      featureHidden: cell.featureHidden,
      territoryStroke: cell.territoryFactionId
        ? Number.parseInt((factionById.get(cell.territoryFactionId)?.color ?? "#ffffff").slice(1), 16)
        : null
    }))
  };
}
