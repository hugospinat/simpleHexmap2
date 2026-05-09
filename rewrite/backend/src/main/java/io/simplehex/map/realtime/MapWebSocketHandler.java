package io.simplehex.map.realtime;

import io.simplehex.map.application.MapCommandException;
import io.simplehex.session.AuthenticatedActor;
import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

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
        AuthenticatedActor actor = extractActor(session);
        if (!actor.isMemberOf(mapId)) {
            throw new MapCommandException(HttpStatus.FORBIDDEN, "map_access_forbidden");
        }
        sessionRegistry.register(mapId, actor, session);
        realtimeNotifier.sendInitialSnapshot(mapId, actor, session);
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

    private AuthenticatedActor extractActor(WebSocketSession session) {
        Object actor = session.getAttributes().get(SessionHandshakeInterceptor.AUTHENTICATED_ACTOR_ATTRIBUTE);
        if (actor instanceof AuthenticatedActor authenticatedActor) {
            return authenticatedActor;
        }
        throw new MapCommandException(HttpStatus.UNAUTHORIZED, "session_required");
    }
}
