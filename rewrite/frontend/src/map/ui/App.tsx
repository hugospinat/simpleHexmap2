import { useEffect, useMemo, useRef } from "react";
import { useMapSlice } from "../application/useMapSlice";
import { projectRenderModel } from "../render/projectRenderModel";
import { mountPreviewScene } from "../render/pixiPreview";
import type { CellDto, MapSnapshotDto, SessionActorDto } from "../transport/dto";

const decisions = [
  "Server authority with last-write-wins sequencing",
  "Exclusive faction territory per cell",
  "No import/export in the first wave",
  "GM can move all tokens; players can move only their own on visible cells"
];

function nextTerritoryFactionId(snapshot: MapSnapshotDto | null, cell: CellDto | null): string | null {
  if (!snapshot || !cell) {
    return null;
  }

  const factionIds = [null, ...snapshot.factions.map((faction) => faction.id)];
  const currentIndex = factionIds.findIndex((factionId) => factionId === cell.territoryFactionId);
  return factionIds[(currentIndex + 1) % factionIds.length];
}

function App() {
  const canvasHostRef = useRef<HTMLDivElement | null>(null);
  const {
    snapshot,
    error,
    isLoading,
    isMutating,
    realtimeStatus,
    session,
    switchActor,
    repaintCell,
    setTerrainVisibility,
    setFeatureVisibility,
    setTerritoryFaction,
    refresh
  } = useMapSlice();
  const renderModel = useMemo(
    () => (snapshot ? projectRenderModel(snapshot) : null),
    [snapshot]
  );

  useEffect(() => {
    const host = canvasHostRef.current;
    if (!host || !renderModel) {
      return;
    }

    const cleanupPromise = mountPreviewScene(host, renderModel);

    return () => {
      void cleanupPromise.then((cleanup) => cleanup());
    };
  }, [renderModel]);

  const activeActor: SessionActorDto | null = session?.currentActor ?? null;
  const firstCell = snapshot?.cells[0] ?? null;
  const canEditMap = activeActor?.role === "gm" || activeActor?.role === "owner";

  return (
    <main className="app-shell">
      <section className="hero-panel">
        <p className="eyebrow">Authoritative editor slice</p>
        <h1>simpleHexmap</h1>
        <p className="lede">
          Current implementation slice: terrain command pipeline, authoritative sequencing,
          a transport snapshot, and a dedicated React plus Pixi render surface.
        </p>
        <ul className="decision-list">
          {decisions.map((decision) => (
            <li key={decision}>{decision}</li>
          ))}
        </ul>
        <div className="control-panel">
          <p className="eyebrow">Session-backed slice</p>
          <div className="role-toggle" role="group" aria-label="Authenticated actor">
            {session?.availableActors.map((actor) => (
              <button
                key={actor.actorId}
                className={`action-button ${activeActor?.actorId === actor.actorId ? "action-button-selected" : "action-button-muted"}`}
                onClick={() => switchActor(actor.actorId)}
                disabled={isLoading || isMutating}
              >
                {actor.displayName} ({actor.role})
              </button>
            ))}
          </div>
          <button className="action-button" onClick={refresh} disabled={isLoading || isMutating}>
            {isLoading ? "Loading snapshot..." : "Reload snapshot"}
          </button>
          <button
            className="action-button action-button-secondary"
            onClick={() => {
              if (firstCell) {
                repaintCell(firstCell.q, firstCell.r, firstCell.terrain === "water" ? "forest" : "water");
              }
            }}
            disabled={!canEditMap || !firstCell || isLoading || isMutating}
          >
            {isMutating ? "Applying terrain..." : "Toggle first cell terrain"}
          </button>
          <button
            className="action-button action-button-secondary"
            onClick={() => {
              if (firstCell) {
                setTerrainVisibility(firstCell.q, firstCell.r, !firstCell.terrainHidden);
              }
            }}
            disabled={!canEditMap || !firstCell || isLoading || isMutating}
          >
            {isMutating ? "Applying fog..." : "Toggle first cell fog"}
          </button>
          <button
            className="action-button action-button-secondary"
            onClick={() => {
              if (firstCell) {
                setFeatureVisibility(firstCell.q, firstCell.r, !firstCell.featureHidden);
              }
            }}
            disabled={!canEditMap || !firstCell || isLoading || isMutating}
          >
            {isMutating ? "Applying feature mask..." : "Toggle first cell feature"}
          </button>
          <button
            className="action-button action-button-secondary"
            onClick={() => {
              if (firstCell) {
                setTerritoryFaction(firstCell.q, firstCell.r, nextTerritoryFactionId(snapshot, firstCell));
              }
            }}
            disabled={!canEditMap || !firstCell || !snapshot || isLoading || isMutating}
          >
            {isMutating ? "Applying territory..." : "Cycle first cell territory"}
          </button>
          <p className="status-line">
            {snapshot
              ? `Loaded ${snapshot.cells.length} cells from ${snapshot.mapId} at revision ${snapshot.revision} as ${activeActor?.displayName ?? "unknown"} (${snapshot.role}).`
              : "No snapshot loaded yet."}
          </p>
          <p className="status-line">Realtime: {realtimeStatus}</p>
          {error ? <p className="error-line">Backend error: {error}</p> : null}
        </div>
      </section>
      <section className="canvas-panel">
        <header>
          <p className="eyebrow">Pixi surface</p>
          <h2>Terrain, visibility, and territory preview</h2>
        </header>
        {!renderModel ? <p className="canvas-empty">Waiting for the backend snapshot.</p> : null}
        <div className="canvas-host" ref={canvasHostRef} />
      </section>
    </main>
  );
}

export default App;
