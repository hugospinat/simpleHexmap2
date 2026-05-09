import type {
  ActorRole,
  CellVisibilityCommandRequestDto,
  CommandAppliedResponseDto,
  MapSnapshotDto,
  RealtimeMessageDto,
  TerrainCommandRequestDto,
  TerrainType
} from "../model/transport";

const API_ROOT = "/api/maps";
const MAP_ID = "demo-map";

function createWebSocketUrl(role: "gm" | "player" | "owner") {
  const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
  return `${protocol}//${window.location.host}${API_ROOT}/${MAP_ID}/ws?role=${role}`;
}

async function parseJsonResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const payload = (await response.json().catch(() => null)) as { error?: string } | null;
    throw new Error(payload?.error ?? `request_failed_${response.status}`);
  }

  return (await response.json()) as T;
}

export async function fetchMapSnapshot(role: "gm" | "player" = "gm"): Promise<MapSnapshotDto> {
  const response = await fetch(`${API_ROOT}/${MAP_ID}?role=${role}`);
  return parseJsonResponse<MapSnapshotDto>(response);
}

export async function applyTerrainCommand(
  operationId: string,
  cell: { q: number; r: number },
  terrain: TerrainType,
  actorRole: ActorRole = "gm"
): Promise<CommandAppliedResponseDto> {
  const command: TerrainCommandRequestDto = {
    type: "set_cell_terrain",
    operationId,
    actorRole,
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
  terrainHidden: boolean,
  actorRole: ActorRole = "gm"
): Promise<CommandAppliedResponseDto> {
  const command: CellVisibilityCommandRequestDto = {
    type: "set_cell_visibility",
    operationId,
    actorRole,
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

export function connectMapRealtime(
  role: ActorRole,
  onMessage: (message: RealtimeMessageDto) => void,
  onClose: () => void,
  onError: (message: string) => void
): WebSocket {
  const socket = new WebSocket(createWebSocketUrl(role));

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