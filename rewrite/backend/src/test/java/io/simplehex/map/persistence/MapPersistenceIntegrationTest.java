package io.simplehex.map.persistence;

import io.simplehex.map.application.MapService;
import io.simplehex.map.bootstrap.DemoMapBootstrap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@AutoConfigureMockMvc
class MapPersistenceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MapService mapService;

    @Autowired
    private DemoMapBootstrap mapDataInitializer;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetMapState() {
        mapService.resetForTests();
        entityManager.clear();
    }

    @Test
    void replaysDuplicateOperationIdWithoutIncrementingRevisionTwice() throws Exception {
        Cookie gmCookie = loginAs("demo-gm");
        String requestBody = """
                {
                  "type": "set_cell_terrain",
                  "operationId": "duplicate-op",
                  "cell": { "q": 0, "r": 0 },
                  "terrain": "water"
                }
                """;

        mockMvc.perform(post("/api/maps/demo-map/commands")
                        .cookie(gmCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sequence").value(1));

        mockMvc.perform(post("/api/maps/demo-map/commands")
                        .cookie(gmCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sequence").value(1))
                .andExpect(jsonPath("$.operationId").value("duplicate-op"));

        entityManager.clear();
        MapEntity map = entityManager.find(MapEntity.class, "demo-map");
        assertThat(map).isNotNull();
        assertThat(map.getRevision()).isEqualTo(1);

        Long operationCount = entityManager.createQuery(
                        "select count(log) from MapOperationLogEntity log where log.id.mapId = :mapId",
                        Long.class)
                .setParameter("mapId", "demo-map")
                .getSingleResult();
        assertThat(operationCount).isEqualTo(1);
    }

    @Test
    void storesOperationLogEntriesInSequenceOrder() throws Exception {
        Cookie gmCookie = loginAs("demo-gm");

        mockMvc.perform(post("/api/maps/demo-map/commands")
                        .cookie(gmCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "set_cell_terrain",
                                  "operationId": "ordered-op-1",
                                  "cell": { "q": 0, "r": 0 },
                                  "terrain": "water"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sequence").value(1));

        mockMvc.perform(post("/api/maps/demo-map/commands")
                        .cookie(gmCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "set_cell_visibility",
                                  "operationId": "ordered-op-2",
                                  "cell": { "q": 1, "r": 0 },
                                  "terrainHidden": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sequence").value(2));

        entityManager.clear();

        List<Long> sequences = entityManager.createQuery(
                        "select log.sequence from MapOperationLogEntity log where log.id.mapId = :mapId order by log.sequence",
                        Long.class)
                .setParameter("mapId", "demo-map")
                .getResultList();
        assertThat(sequences).containsExactly(1L, 2L);

        List<String> commandTypes = entityManager.createQuery(
                        "select log.commandType from MapOperationLogEntity log where log.id.mapId = :mapId order by log.sequence",
                        String.class)
                .setParameter("mapId", "demo-map")
                .getResultList();
        assertThat(commandTypes).containsExactly("set_cell_terrain", "set_cell_visibility");
    }

    @Test
    void seedsDemoMapWhenInitializerRunsAgainstEmptyDatabase() throws Exception {
        clearPersistentState();

        mapDataInitializer.run(new DefaultApplicationArguments(new String[0]));
        entityManager.clear();

        MapEntity map = entityManager.find(MapEntity.class, "demo-map");
        assertThat(map).isNotNull();
        assertThat(map.getRevision()).isZero();

        Long cellCount = entityManager.createQuery("select count(cell) from MapCellEntity cell", Long.class)
                .getSingleResult();
        assertThat(cellCount).isEqualTo(3);
    }

    @Test
    void keepsExistingMapStateWhenInitializerRunsAfterPersistenceAlreadyExists() throws Exception {
        Cookie gmCookie = loginAs("demo-gm");

        mockMvc.perform(post("/api/maps/demo-map/commands")
                        .cookie(gmCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "set_cell_terrain",
                                  "operationId": "restart-op-1",
                                  "cell": { "q": 0, "r": 0 },
                                  "terrain": "water"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sequence").value(1));

        mapDataInitializer.run(new DefaultApplicationArguments(new String[0]));
        entityManager.clear();

        MapEntity map = entityManager.find(MapEntity.class, "demo-map");
        assertThat(map).isNotNull();
        assertThat(map.getRevision()).isEqualTo(1);

        MapCellEntity firstCell = entityManager.find(MapCellEntity.class, new MapCellId("demo-map", 0, 0));
        assertThat(firstCell).isNotNull();
        assertThat(firstCell.getTerrain()).isEqualTo("water");

        Long operationCount = entityManager.createQuery("select count(log) from MapOperationLogEntity log", Long.class)
                .getSingleResult();
        assertThat(operationCount).isEqualTo(1);
    }

    @Test
    void appliesVersionedFlywaySchemaBeforeJpaValidation() {
        List<String> versions = jdbcTemplate.queryForList(
                "select version from flyway_schema_history where success = true and type = 'SQL' order by installed_rank",
                String.class);

        assertThat(versions).containsExactly("1");
    }

    private void clearPersistentState() {
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.createQuery("delete from MapOperationLogEntity").executeUpdate();
            entityManager.createQuery("delete from MapCellEntity").executeUpdate();
            entityManager.createQuery("delete from MapEntity").executeUpdate();
        });
        entityManager.clear();
    }

    private Cookie loginAs(String actorId) throws Exception {
        return mockMvc.perform(post("/api/session/actors/{actorId}", actorId))
                .andReturn()
                .getResponse()
                .getCookie("simplehex_session");
    }
}
