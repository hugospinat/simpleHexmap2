package io.simplehex.map.realtime;

import io.simplehex.map.application.MapService;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MapWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MapService mapService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void resetMapState() {
        mapService.resetForTests();
    }

    @Test
    void websocketReceivesInitialSnapshotAndAppliedCommand() throws Exception {
        BlockingQueue<String> messages = new LinkedBlockingQueue<>();
        WebSocket webSocket = HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .buildAsync(
                        URI.create("ws://localhost:" + port + "/api/maps/demo-map/ws?role=gm"),
                        new QueueingWebSocketListener(messages))
                .join();

        String initialMessage = messages.poll(5, TimeUnit.SECONDS);
        assertThat(initialMessage).isNotNull();
        Map<String, Object> initialPayload = objectMapper.readValue(initialMessage, new TypeReference<>() {
        });
        assertThat(initialPayload.get("type")).isEqualTo("sync_snapshot");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestBody = """
                {
                  "type": "set_cell_terrain",
                  "operationId": "ws-op-1",
                  "actorRole": "gm",
                  "cell": { "q": 0, "r": 0 },
                  "terrain": "water"
                }
                """;

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/maps/demo-map/commands",
                new HttpEntity<>(requestBody, headers),
                String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        String appliedMessage = messages.poll(5, TimeUnit.SECONDS);
        assertThat(appliedMessage).isNotNull();
        Map<String, Object> appliedPayload = objectMapper.readValue(appliedMessage, new TypeReference<>() {
        });
        assertThat(appliedPayload.get("type")).isEqualTo("command_applied");
        assertThat(appliedPayload.get("operationId")).isEqualTo("ws-op-1");
        assertThat(appliedPayload.get("mapId")).isEqualTo("demo-map");
        assertThat(appliedPayload.get("sequence")).isEqualTo(1);

        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "done").join();
    }

    @Test
    void playerWebsocketReceivesFilteredSnapshotAfterVisibilityChange() throws Exception {
        BlockingQueue<String> messages = new LinkedBlockingQueue<>();
        WebSocket webSocket = HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .buildAsync(
                        URI.create("ws://localhost:" + port + "/api/maps/demo-map/ws?role=player"),
                        new QueueingWebSocketListener(messages))
                .join();

        String initialMessage = messages.poll(5, TimeUnit.SECONDS);
        assertThat(initialMessage).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestBody = """
                                {
                                    "type": "set_cell_visibility",
                                    "operationId": "ws-visibility-1",
                                    "actorRole": "gm",
                                    "cell": { "q": 0, "r": 0 },
                                    "terrainHidden": true
                                }
                                """;

        ResponseEntity<String> response = restTemplate.postForEntity(
                                "http://localhost:" + port + "/api/maps/demo-map/commands",
                                new HttpEntity<>(requestBody, headers),
                                String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        String snapshotMessage = messages.poll(5, TimeUnit.SECONDS);
        assertThat(snapshotMessage).isNotNull();
        Map<String, Object> snapshotPayload = objectMapper.readValue(snapshotMessage, new TypeReference<>() {
        });
        assertThat(snapshotPayload.get("type")).isEqualTo("sync_snapshot");
        Map<?, ?> snapshot = (Map<?, ?>) snapshotPayload.get("snapshot");
        assertThat(snapshot.get("revision")).isEqualTo(1);
        assertThat(((java.util.List<?>) snapshot.get("cells"))).hasSize(2);

        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "done").join();
    }

    private static final class QueueingWebSocketListener implements Listener {
        private final BlockingQueue<String> messages;
        private final StringBuilder currentMessage = new StringBuilder();

        private QueueingWebSocketListener(BlockingQueue<String> messages) {
            this.messages = messages;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            Listener.super.onOpen(webSocket);
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            currentMessage.append(data);
            if (last) {
                messages.add(currentMessage.toString());
                currentMessage.setLength(0);
            }
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }
    }
}