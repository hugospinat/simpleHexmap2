export type TerrainType = "forest" | "hills" | "water" | "plains";

export type ActorRole = "gm" | "player" | "owner";

export type CellDto = {
  q: number;
  r: number;
  terrain: TerrainType;
  terrainHidden: boolean;
  featureHidden: boolean;
  territoryFactionId: string | null;
};

export type FactionDto = {
  id: string;
  label: string;
  color: string;
};

export type MapSnapshotDto = {
  mapId: string;
  revision: number;
  role: "gm" | "player";
  factions: FactionDto[];
  cells: CellDto[];
};

export type CellRefDto = {
  q: number;
  r: number;
};

export type TerrainCommandRequestDto = {
  type: "set_cell_terrain";
  operationId: string;
  actorRole: ActorRole;
  cell: CellRefDto;
  terrain: TerrainType;
};

export type CellVisibilityCommandRequestDto = {
  type: "set_cell_visibility";
  operationId: string;
  actorRole: ActorRole;
  cell: CellRefDto;
  terrainHidden: boolean;
};

export type FeatureVisibilityCommandRequestDto = {
  type: "set_cell_feature_visibility";
  operationId: string;
  actorRole: ActorRole;
  cell: CellRefDto;
  featureHidden: boolean;
};

export type TerritoryCommandRequestDto = {
  type: "set_cell_territory";
  operationId: string;
  actorRole: ActorRole;
  cell: CellRefDto;
  territoryFactionId: string | null;
};

export type AppliedCommandDto =
  | {
      type: "set_cell_terrain";
      cell: CellRefDto;
      terrain: TerrainType;
      terrainHidden: null;
      featureHidden: null;
      territoryFactionId: null;
    }
  | {
      type: "set_cell_visibility";
      cell: CellRefDto;
      terrain: null;
      terrainHidden: boolean;
      featureHidden: null;
      territoryFactionId: null;
    }
  | {
      type: "set_cell_feature_visibility";
      cell: CellRefDto;
      terrain: null;
      terrainHidden: null;
      featureHidden: boolean;
      territoryFactionId: null;
    }
  | {
      type: "set_cell_territory";
      cell: CellRefDto;
      terrain: null;
      terrainHidden: null;
      featureHidden: null;
      territoryFactionId: string | null;
    };

export type CommandAppliedResponseDto = {
  type: "command_applied";
  operationId: string;
  mapId: string;
  sequence: number;
  command: AppliedCommandDto;
};

export type SyncSnapshotMessageDto = {
  type: "sync_snapshot";
  snapshot: MapSnapshotDto;
};

export type RealtimeMessageDto = SyncSnapshotMessageDto | CommandAppliedResponseDto;
