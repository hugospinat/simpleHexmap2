package io.simplehex.map.realtime;

import io.simplehex.map.application.MapCommandException;
import io.simplehex.map.domain.ActorRole;
import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class MapWebSocketHandler extends TextWebSocketHandler {

    private final MapRealtimeSessionRegistry sessionRegistry;
    private final MapRealtimeNotifier realtimeNotifier;

    public MapWebSocketHandler(MapRealtimeSessionRegistry sessionRegistry, MapRealtimeNotifier realtimeNotifier) {
        this.sessionRegistry = sessionRegistry;
        this.realtimeNotifier = realtimeNotifier;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String mapId = extractMapId(session);
        ActorRole role = extractRole(session);
        sessionRegistry.register(mapId, role, session);
        realtimeNotifier.sendInitialSnapshot(mapId, role, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionRegistry.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        sessionRegistry.remove(session);
    }

    private String extractMapId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            throw new MapCommandException(org.springframework.http.HttpStatus.BAD_REQUEST, "missing_websocket_uri");
        }

        List<String> segments = List.of(uri.getPath().split("/"));
        if (segments.size() < 4) {
            throw new MapCommandException(org.springframework.http.HttpStatus.BAD_REQUEST, "invalid_websocket_path");
        }

        return segments.get(segments.size() - 2);
    }

    private ActorRole extractRole(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            return ActorRole.GM;
        }

        String role = UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst("role");
        return role == null ? ActorRole.GM : ActorRole.fromValue(role);
    }
}