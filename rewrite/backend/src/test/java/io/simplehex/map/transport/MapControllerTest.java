package io.simplehex.map.transport;

import io.simplehex.map.application.MapService;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MapService mapService;

    @BeforeEach
    void resetMapState() {
        mapService.resetForTests();
    }

    @Test
    void returnsSeededSnapshot() throws Exception {
        mockMvc.perform(get("/api/maps/demo-map").queryParam("role", "gm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mapId").value("demo-map"))
                .andExpect(jsonPath("$.revision").value(0))
                .andExpect(jsonPath("$.factions.length()").value(2))
                .andExpect(jsonPath("$.cells.length()").value(3))
                .andExpect(jsonPath("$.cells[0].terrain").value("plains"))
                .andExpect(jsonPath("$.cells[1].territoryFactionId").value("amber"));
    }

    @Test
    void appliesTerrainCommandAndIncrementsSequence() throws Exception {
        String requestBody = """
                {
                  "type": "set_cell_terrain",
                  "operationId": "op-1",
                  "actorRole": "gm",
                  "cell": { "q": 0, "r": 0 },
                  "terrain": "water"
                }
                """;

        mockMvc.perform(post("/api/maps/demo-map/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("command_applied"))
                .andExpect(jsonPath("$.operationId").value("op-1"))
                .andExpect(jsonPath("$.sequence").value(1))
                .andExpect(jsonPath("$.command.type").value("set_cell_terrain"))
                .andExpect(jsonPath("$.command.terrain").value("water"));

        mockMvc.perform(get("/api/maps/demo-map").queryParam("role", "gm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value(1))
                .andExpect(jsonPath("$.cells[0].terrain").value("water"));
    }

    @Test
    void rejectsTerrainEditsFromPlayers() throws Exception {
        String requestBody = """
                {
                  "type": "set_cell_terrain",
                  "operationId": "op-2",
                  "actorRole": "player",
                  "cell": { "q": 0, "r": 0 },
                  "terrain": "water"
                }
                """;

        mockMvc.perform(post("/api/maps/demo-map/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("terrain_edit_forbidden"));
    }

        @Test
    void appliesVisibilityCommandAndFiltersPlayerSnapshot() throws Exception {
            String requestBody = """
                {
                  "type": "set_cell_visibility",
                  "operationId": "op-visibility-1",
                  "actorRole": "gm",
                  "cell": { "q": 0, "r": 0 },
                  "terrainHidden": true
                }
                """;

        mockMvc.perform(post("/api/maps/demo-map/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationId").value("op-visibility-1"))
                .andExpect(jsonPath("$.command.type").value("set_cell_visibility"))
                .andExpect(jsonPath("$.command.terrainHidden").value(true));

        mockMvc.perform(get("/api/maps/demo-map").queryParam("role", "player"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value(1))
                .andExpect(jsonPath("$.cells.length()").value(2));
    }

    @Test
    void appliesFeatureVisibilityCommandAndKeepsCellInSnapshot() throws Exception {
        String requestBody = """
                {
                  "type": "set_cell_feature_visibility",
                  "operationId": "op-feature-1",
                  "actorRole": "gm",
                  "cell": { "q": 1, "r": 0 },
                  "featureHidden": true
                }
                """;

        mockMvc.perform(post("/api/maps/demo-map/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationId").value("op-feature-1"))
                .andExpect(jsonPath("$.command.type").value("set_cell_feature_visibility"))
                .andExpect(jsonPath("$.command.featureHidden").value(true));

        mockMvc.perform(get("/api/maps/demo-map").queryParam("role", "gm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value(1))
                .andExpect(jsonPath("$.cells[1].featureHidden").value(true));
    }

    @Test
    void appliesTerritoryCommandAndPersistsFactionOwnership() throws Exception {
        String requestBody = """
                {
                  "type": "set_cell_territory",
                  "operationId": "op-territory-1",
                  "actorRole": "gm",
                  "cell": { "q": 0, "r": 0 },
                  "territoryFactionId": "violet"
                }
                """;

        mockMvc.perform(post("/api/maps/demo-map/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationId").value("op-territory-1"))
                .andExpect(jsonPath("$.command.type").value("set_cell_territory"))
                .andExpect(jsonPath("$.command.territoryFactionId").value("violet"));

        mockMvc.perform(get("/api/maps/demo-map").queryParam("role", "gm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value(1))
                .andExpect(jsonPath("$.cells[0].territoryFactionId").value("violet"));
    }
}
