package io.simplehex.map.realtime;

import io.simplehex.session.AuthenticatedActor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class MapRealtimeSessionRegistry {

    private final Map<String, CopyOnWriteArrayList<MapWebSocketSession>> sessionsByMap = new ConcurrentHashMap<>();

    public void register(String mapId, AuthenticatedActor actor, WebSocketSession session) {
        sessionsByMap.computeIfAbsent(mapId, ignored -> new CopyOnWriteArrayList<>())
                .add(new MapWebSocketSession(actor, session));
    }

    public void remove(WebSocketSession session) {
        sessionsByMap.values().forEach(sessions -> sessions.removeIf(entry -> entry.session().getId().equals(session.getId())));
        sessionsByMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public List<MapWebSocketSession> sessionsForMap(String mapId) {
        return List.copyOf(sessionsByMap.getOrDefault(mapId, new CopyOnWriteArrayList<>()));
    }

    public record MapWebSocketSession(AuthenticatedActor actor, WebSocketSession session) {
    }
}
