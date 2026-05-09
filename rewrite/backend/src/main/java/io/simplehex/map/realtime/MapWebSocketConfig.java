package io.simplehex.map.realtime;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class MapWebSocketConfig implements WebSocketConfigurer {

    private final MapWebSocketHandler mapWebSocketHandler;
    private final SessionHandshakeInterceptor sessionHandshakeInterceptor;

    public MapWebSocketConfig(
            MapWebSocketHandler mapWebSocketHandler,
            SessionHandshakeInterceptor sessionHandshakeInterceptor
    ) {
        this.mapWebSocketHandler = mapWebSocketHandler;
        this.sessionHandshakeInterceptor = sessionHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(mapWebSocketHandler, "/api/maps/*/ws")
                .addInterceptors(sessionHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
