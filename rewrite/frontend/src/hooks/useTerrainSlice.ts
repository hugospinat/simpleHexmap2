import { useEffect, useRef, useState } from "react";
import { applyTerrainCommand, applyVisibilityCommand, connectMapRealtime, fetchMapSnapshot } from "../api/mapApi";
import { applyOptimisticTerrain } from "../model/applyOptimisticTerrain";
import { applyOptimisticVisibility } from "../model/applyOptimisticVisibility";
import { applyRealtimeMessage } from "../model/applyRealtimeMessage";
import type { ActorRole, MapSnapshotDto, TerrainType } from "../model/transport";

type RealtimeStatus = "connecting" | "open" | "closed" | "error";

type TerrainSliceState = {
  snapshot: MapSnapshotDto | null;
  error: string | null;
  isLoading: boolean;
  isMutating: boolean;
  realtimeStatus: RealtimeStatus;
  role: "gm" | "player";
  setRole: (role: "gm" | "player") => void;
  repaintCell: (q: number, r: number, terrain: TerrainType) => void;
  setTerrainVisibility: (q: number, r: number, terrainHidden: boolean) => void;
  refresh: () => void;
};

export function useTerrainSlice(): TerrainSliceState {
  const [snapshot, setSnapshot] = useState<MapSnapshotDto | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [realtimeStatus, setRealtimeStatus] = useState<RealtimeStatus>("connecting");
  const [pendingOperationIds, setPendingOperationIds] = useState<string[]>([]);
  const [role, setRole] = useState<"gm" | "player">("gm");
  const socketRef = useRef<WebSocket | null>(null);

  const loadSnapshot = () => {
    setIsLoading(true);
    setError(null);

    void fetchMapSnapshot(role)
      .then((nextSnapshot) => {
        setSnapshot(nextSnapshot);
      })
      .catch((loadError: Error) => {
        setError(loadError.message);
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  useEffect(() => {
    loadSnapshot();
  }, [role]);

  useEffect(() => {
    setRealtimeStatus("connecting");
    const socket = connectMapRealtime(
      role,
      (message) => {
        setRealtimeStatus("open");
        if (message.type === "command_applied") {
          setPendingOperationIds((current) =>
            current.filter((operationId) => operationId !== message.operationId)
          );
        }
        setSnapshot((currentSnapshot) => applyRealtimeMessage(currentSnapshot, message));
      },
      () => {
        setRealtimeStatus("closed");
      },
      (message) => {
        setRealtimeStatus("error");
        setError(message);
      }
    );

    socketRef.current = socket;

    return () => {
      socket.close();
      socketRef.current = null;
    };
  }, [role]);

  const repaintCell = (q: number, r: number, terrain: TerrainType) => {
    const operationId = crypto.randomUUID();
    setError(null);
    setPendingOperationIds((current) => [...current, operationId]);
    setSnapshot((currentSnapshot) => applyOptimisticTerrain(currentSnapshot, q, r, terrain));

    void applyTerrainCommand(operationId, { q, r }, terrain, role).catch((commandError: Error) => {
      setPendingOperationIds((current) => current.filter((value) => value !== operationId));
      setError(commandError.message);
      loadSnapshot();
    });
  };

  const setTerrainVisibility = (q: number, r: number, terrainHidden: boolean) => {
    const operationId = crypto.randomUUID();
    setError(null);
    setPendingOperationIds((current) => [...current, operationId]);
    setSnapshot((currentSnapshot) => applyOptimisticVisibility(currentSnapshot, q, r, terrainHidden, role));

    void applyVisibilityCommand(operationId, { q, r }, terrainHidden, role).catch((commandError: Error) => {
      setPendingOperationIds((current) => current.filter((value) => value !== operationId));
      setError(commandError.message);
      loadSnapshot();
    });
  };

  return {
    snapshot,
    error,
    isLoading,
    isMutating: pendingOperationIds.length > 0,
    realtimeStatus,
    role,
    setRole,
    repaintCell,
    setTerrainVisibility,
    refresh: loadSnapshot
  };
}