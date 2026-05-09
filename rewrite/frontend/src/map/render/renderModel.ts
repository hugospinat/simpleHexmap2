import type { TerrainType } from "../transport/dto";

export type TerrainTileRender = {
  q: number;
  r: number;
  terrain: TerrainType;
  fill: number;
  label: string;
  detailLabel: string;
  terrainHidden: boolean;
  featureHidden: boolean;
  territoryStroke: number | null;
};

export type RenderModel = {
  title: string;
  subtitle: string;
  terrainTiles: TerrainTileRender[];
};
