import type { TerrainType } from "./transport";

export type TerrainTileRender = {
  q: number;
  r: number;
  terrain: TerrainType;
  fill: number;
  label: string;
  terrainHidden: boolean;
};

export type RenderModel = {
  title: string;
  subtitle: string;
  terrainTiles: TerrainTileRender[];
};