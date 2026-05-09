import { describe, expect, it } from "vitest";
import { applyOptimisticFeatureVisibility, applyOptimisticVisibility } from "./applyOptimisticVisibility";
import { applyOptimisticTerrain } from "./applyOptimisticTerrain";
import { applyOptimisticTerritory } from "./applyOptimisticTerritory";
import { applyRealtimeMessage } from "./applyRealtimeMessage";
import type { ActorRole, MapSnapshotDto, RealtimeMessageDto } from "../transport/dto";

function createSnapshot(role: ActorRole = "gm"): MapSnapshotDto {
  return {
    mapId: "demo-map",
    revision: 1,
    role,
    factions: [
      { id: "amber", label: "Amber", color: "#f7b733" },
      { id: "violet", label: "Violet", color: "#8f63ff" }
    ],
    cells: [
      {
        q: 0,
        r: 0,
        terrain: "plains",
        terrainHidden: false,
        featureHidden: false,
        territoryFactionId: null
      },
      {
        q: 1,
        r: 0,
        terrain: "forest",
        terrainHidden: false,
        featureHidden: false,
        territoryFactionId: "amber"
      }
    ]
  };
}

describe("map application slice reducers", () => {
  it("applies optimistic terrain updates to the targeted cell", () => {
    const nextSnapshot = applyOptimisticTerrain(createSnapshot(), 0, 0, "water");

    expect(nextSnapshot?.cells[0]?.terrain).toBe("water");
    expect(nextSnapshot?.cells[1]?.terrain).toBe("forest");
  });

  it("removes terrain-hidden cells from player snapshots optimistically", () => {
    const nextSnapshot = applyOptimisticVisibility(createSnapshot("player"), 0, 0, true, "player");

    expect(nextSnapshot?.cells).toEqual([
      {
        q: 1,
        r: 0,
        terrain: "forest",
        terrainHidden: false,
        featureHidden: false,
        territoryFactionId: "amber"
      }
    ]);
  });

  it("keeps gm cells in place when toggling terrain visibility optimistically", () => {
    const nextSnapshot = applyOptimisticVisibility(createSnapshot(), 0, 0, true, "gm");

    expect(nextSnapshot?.cells[0]?.terrainHidden).toBe(true);
    expect(nextSnapshot?.cells).toHaveLength(2);
  });

  it("applies optimistic feature visibility and territory ownership updates", () => {
    const featureHiddenSnapshot = applyOptimisticFeatureVisibility(createSnapshot(), 1, 0, true);
    const territorySnapshot = applyOptimisticTerritory(featureHiddenSnapshot, 0, 0, "violet");

    expect(featureHiddenSnapshot?.cells[1]?.featureHidden).toBe(true);
    expect(territorySnapshot?.cells[0]?.territoryFactionId).toBe("violet");
  });

  it("reconciles optimistic player state with an authoritative sync snapshot", () => {
    const optimisticSnapshot = applyOptimisticVisibility(createSnapshot("player"), 0, 0, true, "player");
    const authoritativeSnapshot: MapSnapshotDto = {
      ...createSnapshot("player"),
      revision: 2,
      cells: [
        {
          q: 0,
          r: 0,
          terrain: "plains",
          terrainHidden: false,
          featureHidden: false,
          territoryFactionId: null
        },
        {
          q: 1,
          r: 0,
          terrain: "forest",
          terrainHidden: false,
          featureHidden: true,
          territoryFactionId: "amber"
        }
      ]
    };

    const reconciledSnapshot = applyRealtimeMessage(optimisticSnapshot, {
      type: "sync_snapshot",
      snapshot: authoritativeSnapshot
    });

    expect(reconciledSnapshot).toEqual(authoritativeSnapshot);
  });

  it("applies authoritative realtime command updates with the server sequence", () => {
    const realtimeMessage: RealtimeMessageDto = {
      type: "command_applied",
      operationId: "op-1",
      mapId: "demo-map",
      sequence: 2,
      command: {
        type: "set_cell_terrain",
        cell: { q: 0, r: 0 },
        terrain: "hills",
        terrainHidden: null,
        featureHidden: null,
        territoryFactionId: null
      }
    };

    const nextSnapshot = applyRealtimeMessage(createSnapshot(), realtimeMessage);

    expect(nextSnapshot?.revision).toBe(2);
    expect(nextSnapshot?.cells[0]?.terrain).toBe("hills");
  });
});
