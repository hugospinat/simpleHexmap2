import type {
  CellVisibilityCommandRequestDto,
  CommandAppliedResponseDto,
  FeatureVisibilityCommandRequestDto,
  MapSnapshotDto,
  RealtimeMessageDto,
  SessionDto,
  TerrainCommandRequestDto,
  TerrainType,
  TerritoryCommandRequestDto
} from "./dto";

const API_ROOT = "/api/maps";
const MAP_ID = "demo-map";

function createWebSocketUrl() {
  const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
  return `${protocol}//${window.location.host}${API_ROOT}/${MAP_ID}/ws`;
}

async function parseJsonResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const payload = (await response.json().catch(() => null)) as { error?: string } | null;
    throw new Error(payload?.error ?? `request_failed_${response.status}`);
  }

  return (await response.json()) as T;
}

export async function fetchSession(): Promise<SessionDto> {
  const response = await fetch("/api/session");
  return parseJsonResponse<SessionDto>(response);
}

export async function activateSession(actorId: string): Promise<SessionDto> {
  const response = await fetch(`/api/session/actors/${actorId}`, { method: "POST" });
  return parseJsonResponse<SessionDto>(response);
}

export async function fetchMapSnapshot(): Promise<MapSnapshotDto> {
  const response = await fetch(`${API_ROOT}/${MAP_ID}`);
  return parseJsonResponse<MapSnapshotDto>(response);
}

export async function applyTerrainCommand(
  operationId: string,
  cell: { q: number; r: number },
  terrain: TerrainType
): Promise<CommandAppliedResponseDto> {
  const command: TerrainCommandRequestDto = {
    type: "set_cell_terrain",
    operationId,
    cell,
    terrain
  };

  const response = await fetch(`${API_ROOT}/${MAP_ID}/commands`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(command)
  });

  return parseJsonResponse<CommandAppliedResponseDto>(response);
}

export async function applyVisibilityCommand(
  operationId: string,
  cell: { q: number; r: number },
  terrainHidden: boolean
): Promise<CommandAppliedResponseDto> {
  const command: CellVisibilityCommandRequestDto = {
    type: "set_cell_visibility",
    operationId,
    cell,
    terrainHidden
  };

  const response = await fetch(`${API_ROOT}/${MAP_ID}/commands`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(command)
  });

  return parseJsonResponse<CommandAppliedResponseDto>(response);
}

export async function applyFeatureVisibilityCommand(
  operationId: string,
  cell: { q: number; r: number },
  featureHidden: boolean
): Promise<CommandAppliedResponseDto> {
  const command: FeatureVisibilityCommandRequestDto = {
    type: "set_cell_feature_visibility",
    operationId,
    cell,
    featureHidden
  };

  const response = await fetch(`${API_ROOT}/${MAP_ID}/commands`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(command)
  });

  return parseJsonResponse<CommandAppliedResponseDto>(response);
}

export async function applyTerritoryCommand(
  operationId: string,
  cell: { q: number; r: number },
  territoryFactionId: string | null
): Promise<CommandAppliedResponseDto> {
  const command: TerritoryCommandRequestDto = {
    type: "set_cell_territory",
    operationId,
    cell,
    territoryFactionId
  };

  const response = await fetch(`${API_ROOT}/${MAP_ID}/commands`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(command)
  });

  return parseJsonResponse<CommandAppliedResponseDto>(response);
}

export function connectMapRealtime(
  onMessage: (message: RealtimeMessageDto) => void,
  onClose: () => void,
  onError: (message: string) => void
): WebSocket {
  const socket = new WebSocket(createWebSocketUrl());

  socket.addEventListener("message", (event) => {
    try {
      onMessage(JSON.parse(event.data) as RealtimeMessageDto);
    } catch {
      onError("invalid_realtime_payload");
    }
  });

  socket.addEventListener("close", onClose);
  socket.addEventListener("error", () => {
    onError("realtime_connection_failed");
  });

  return socket;
}
