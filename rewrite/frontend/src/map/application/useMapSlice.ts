import { useEffect, useRef, useState } from "react";
import {
  activateSession,
  applyFeatureVisibilityCommand,
  applyTerrainCommand,
  applyTerritoryCommand,
  applyVisibilityCommand,
  connectMapRealtime,
  fetchSession,
  fetchMapSnapshot
} from "../transport/mapApi";
import { applyOptimisticTerritory } from "./applyOptimisticTerritory";
import { applyOptimisticFeatureVisibility, applyOptimisticVisibility } from "./applyOptimisticVisibility";
import { applyOptimisticTerrain } from "./applyOptimisticTerrain";
import { applyRealtimeMessage } from "./applyRealtimeMessage";
import type { MapSnapshotDto, SessionActorDto, SessionDto, TerrainType } from "../transport/dto";

type RealtimeStatus = "connecting" | "open" | "closed" | "error";

type MapSliceState = {
  snapshot: MapSnapshotDto | null;
  error: string | null;
  isLoading: boolean;
  isMutating: boolean;
  realtimeStatus: RealtimeStatus;
  session: SessionDto | null;
  switchActor: (actorId: string) => void;
  repaintCell: (q: number, r: number, terrain: TerrainType) => void;
  setTerrainVisibility: (q: number, r: number, terrainHidden: boolean) => void;
  setFeatureVisibility: (q: number, r: number, featureHidden: boolean) => void;
  setTerritoryFaction: (q: number, r: number, territoryFactionId: string | null) => void;
  refresh: () => void;
};

export function useMapSlice(): MapSliceState {
  const [snapshot, setSnapshot] = useState<MapSnapshotDto | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [realtimeStatus, setRealtimeStatus] = useState<RealtimeStatus>("connecting");
  const [pendingOperationIds, setPendingOperationIds] = useState<string[]>([]);
  const [session, setSession] = useState<SessionDto | null>(null);
  const socketRef = useRef<WebSocket | null>(null);
  const activeActor: SessionActorDto | null = session?.currentActor ?? null;

  const loadSnapshot = () => {
    if (!activeActor) {
      return;
    }
    setIsLoading(true);
    setError(null);

    void fetchMapSnapshot()
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
    setIsLoading(true);
    setError(null);
    void fetchSession()
      .then((nextSession) => {
        setSession(nextSession);
      })
      .catch((loadError: Error) => {
        setError(loadError.message);
        setIsLoading(false);
      });
  }, []);

  useEffect(() => {
    if (!activeActor) {
      return;
    }
    loadSnapshot();
  }, [activeActor?.actorId]);

  useEffect(() => {
    if (!activeActor) {
      return;
    }
    setRealtimeStatus("connecting");
    const socket = connectMapRealtime(
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
  }, [activeActor?.actorId]);

  const switchActor = (actorId: string) => {
    setIsLoading(true);
    setError(null);
    setSnapshot(null);

    void activateSession(actorId)
      .then((nextSession) => {
        setSession(nextSession);
      })
      .catch((commandError: Error) => {
        setError(commandError.message);
        setIsLoading(false);
      });
  };

  const repaintCell = (q: number, r: number, terrain: TerrainType) => {
    if (!activeActor) {
      return;
    }
    const operationId = crypto.randomUUID();
    setError(null);
    setPendingOperationIds((current) => [...current, operationId]);
    setSnapshot((currentSnapshot) => applyOptimisticTerrain(currentSnapshot, q, r, terrain));

    void applyTerrainCommand(operationId, { q, r }, terrain).catch((commandError: Error) => {
      setPendingOperationIds((current) => current.filter((value) => value !== operationId));
      setError(commandError.message);
      loadSnapshot();
    });
  };

  const setTerrainVisibility = (q: number, r: number, terrainHidden: boolean) => {
    if (!activeActor) {
      return;
    }
    const operationId = crypto.randomUUID();
    setError(null);
    setPendingOperationIds((current) => [...current, operationId]);
    setSnapshot((currentSnapshot) =>
      applyOptimisticVisibility(currentSnapshot, q, r, terrainHidden, activeActor.role)
    );

    void applyVisibilityCommand(operationId, { q, r }, terrainHidden).catch((commandError: Error) => {
      setPendingOperationIds((current) => current.filter((value) => value !== operationId));
      setError(commandError.message);
      loadSnapshot();
    });
  };

  const setFeatureVisibility = (q: number, r: number, featureHidden: boolean) => {
    if (!activeActor) {
      return;
    }
    const operationId = crypto.randomUUID();
    setError(null);
    setPendingOperationIds((current) => [...current, operationId]);
    setSnapshot((currentSnapshot) =>
      applyOptimisticFeatureVisibility(currentSnapshot, q, r, featureHidden)
    );

    void applyFeatureVisibilityCommand(operationId, { q, r }, featureHidden).catch(
      (commandError: Error) => {
        setPendingOperationIds((current) => current.filter((value) => value !== operationId));
        setError(commandError.message);
        loadSnapshot();
      }
    );
  };

  const setTerritoryFaction = (q: number, r: number, territoryFactionId: string | null) => {
    if (!activeActor) {
      return;
    }
    const operationId = crypto.randomUUID();
    setError(null);
    setPendingOperationIds((current) => [...current, operationId]);
    setSnapshot((currentSnapshot) =>
      applyOptimisticTerritory(currentSnapshot, q, r, territoryFactionId)
    );

    void applyTerritoryCommand(operationId, { q, r }, territoryFactionId).catch(
      (commandError: Error) => {
        setPendingOperationIds((current) => current.filter((value) => value !== operationId));
        setError(commandError.message);
        loadSnapshot();
      }
    );
  };

  return {
    snapshot,
    error,
    isLoading,
    isMutating: pendingOperationIds.length > 0,
    realtimeStatus,
    session,
    switchActor,
    repaintCell,
    setTerrainVisibility,
    setFeatureVisibility,
    setTerritoryFaction,
    refresh: loadSnapshot
  };
}
