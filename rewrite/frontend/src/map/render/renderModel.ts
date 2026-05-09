import type { TerrainType } from "../transport/dto";

export type TerrainTileRender = {
  q: number;
  r: number;
  terrain: TerrainType;
  fill: number;
  label: string;
  visibilityLabel: string;
  terrainHidden: boolean;
  featureHidden: boolean;
};

export type RenderModel = {
  title: string;
  subtitle: string;
  terrainTiles: TerrainTileRender[];
};
