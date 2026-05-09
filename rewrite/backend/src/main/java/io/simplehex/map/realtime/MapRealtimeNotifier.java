package io.simplehex.map.realtime;

import io.simplehex.map.application.MapCommandAppliedEvent;
import io.simplehex.map.application.MapCommandException;
import io.simplehex.map.application.MapService;
import io.simplehex.map.domain.ActorRole;
import io.simplehex.session.AuthenticatedActor;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class MapRealtimeNotifier {

    private final MapService mapService;
    private final MapRealtimeSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    public MapRealtimeNotifier(
        MapService mapService,
            MapRealtimeSessionRegistry sessionRegistry,
            ObjectMapper objectMapper
    ) {
        this.mapService = mapService;
        this.sessionRegistry = sessionRegistry;
        this.objectMapper = objectMapper;
    }

    @EventListener
    public void onCommandApplied(MapCommandAppliedEvent event) {
        for (MapRealtimeSessionRegistry.MapWebSocketSession target : sessionRegistry.sessionsForMap(event.mapId())) {
            if (!target.session().isOpen()) {
                sessionRegistry.remove(target.session());
                continue;
            }

            Object payload = target.actor().role() == ActorRole.PLAYER
                    ? new SyncSnapshotMessage("sync_snapshot", mapService.getSnapshot(event.mapId(), target.actor()))
                    : event.response();

            sendJson(target.session(), payload);
        }
    }

    public void sendInitialSnapshot(String mapId, AuthenticatedActor actor, WebSocketSession session) {
        SyncSnapshotMessage snapshotMessage = new SyncSnapshotMessage(
                "sync_snapshot",
                mapService.getSnapshot(mapId, actor));
        sendJson(session, snapshotMessage);
    }

    private void sendJson(WebSocketSession session, Object payload) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
        } catch (JsonProcessingException exception) {
            throw new MapCommandException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "realtime_serialization_failed");
        } catch (IOException exception) {
            sessionRegistry.remove(session);
        }
    }
}
