package io.simplehex.map.realtime;

import io.simplehex.session.AuthenticatedActor;
import io.simplehex.session.DemoSessionService;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
public class SessionHandshakeInterceptor implements HandshakeInterceptor {

    public static final String AUTHENTICATED_ACTOR_ATTRIBUTE = "authenticatedActor";

    private final DemoSessionService demoSessionService;

    public SessionHandshakeInterceptor(DemoSessionService demoSessionService) {
        this.demoSessionService = demoSessionService;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        AuthenticatedActor actor = demoSessionService.resolveFromCookieHeaders(request.getHeaders().get(HttpHeaders.COOKIE))
                .orElse(null);
        if (actor == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put(AUTHENTICATED_ACTOR_ATTRIBUTE, actor);
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
    }
}
