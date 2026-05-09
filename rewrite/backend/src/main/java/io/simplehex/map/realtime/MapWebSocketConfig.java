package io.simplehex.map.realtime;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class MapWebSocketConfig implements WebSocketConfigurer {

    private final MapWebSocketHandler mapWebSocketHandler;

    public MapWebSocketConfig(MapWebSocketHandler mapWebSocketHandler) {
        this.mapWebSocketHandler = mapWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(mapWebSocketHandler, "/api/maps/*/ws")
                .setAllowedOrigins("*");
    }
}