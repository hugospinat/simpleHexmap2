package io.simplehex.session;

import io.simplehex.map.application.MapCommandException;
import io.simplehex.map.domain.ActorRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class DemoSessionService {

    public static final String SESSION_COOKIE_NAME = "simplehex_session";

    private static final String DEFAULT_ACTOR_ID = "demo-gm";

    private final Map<String, SessionActorDefinition> actorsById = new LinkedHashMap<>();
    private final Map<String, AuthenticatedActor> sessionsById = new ConcurrentHashMap<>();

    public DemoSessionService() {
        actorsById.put("demo-gm", new SessionActorDefinition("demo-gm", "Maris the GM", ActorRole.GM, Set.of("demo-map")));
        actorsById.put("demo-player", new SessionActorDefinition("demo-player", "Iven the Scout", ActorRole.PLAYER, Set.of("demo-map")));
    }

    public SessionResponse getOrCreateSession(HttpServletRequest request, HttpServletResponse response) {
        return resolveFromServletRequest(request)
                .map(this::toResponse)
                .orElseGet(() -> activate(DEFAULT_ACTOR_ID, response));
    }

    public SessionResponse activate(String actorId, HttpServletResponse response) {
        SessionActorDefinition actor = actorsById.get(actorId);
        if (actor == null) {
            throw new MapCommandException(HttpStatus.BAD_REQUEST, "unknown_actor_id");
        }

        String sessionId = UUID.randomUUID().toString();
        AuthenticatedActor authenticatedActor = new AuthenticatedActor(
                sessionId,
                actor.actorId(),
                actor.displayName(),
                actor.role(),
                actor.mapMemberships());
        sessionsById.put(sessionId, authenticatedActor);
        writeSessionCookie(response, sessionId);
        return toResponse(authenticatedActor);
    }

    public AuthenticatedActor requireActor(HttpServletRequest request, String mapId) {
        AuthenticatedActor actor = resolveFromServletRequest(request)
                .orElseThrow(() -> new MapCommandException(HttpStatus.UNAUTHORIZED, "session_required"));
        if (!actor.isMemberOf(mapId)) {
            throw new MapCommandException(HttpStatus.FORBIDDEN, "map_access_forbidden");
        }
        return actor;
    }

    public Optional<AuthenticatedActor> resolveFromCookieHeaders(List<String> cookieHeaders) {
        if (cookieHeaders == null || cookieHeaders.isEmpty()) {
            return Optional.empty();
        }

        return cookieHeaders.stream()
                .flatMap(header -> Arrays.stream(header.split(";")))
                .map(String::trim)
                .filter(cookie -> cookie.startsWith(SESSION_COOKIE_NAME + "="))
                .findFirst()
                .map(cookie -> cookie.substring((SESSION_COOKIE_NAME + "=").length()))
                .flatMap(sessionId -> Optional.ofNullable(sessionsById.get(sessionId)));
    }

    private Optional<AuthenticatedActor> resolveFromServletRequest(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> SESSION_COOKIE_NAME.equals(cookie.getName()))
                .findFirst()
                .map(cookie -> sessionsById.get(cookie.getValue()));
    }

    private SessionResponse toResponse(AuthenticatedActor actor) {
        return new SessionResponse(toActorResponse(actor), actorsById.values().stream()
                .map(this::toActorResponse)
                .toList());
    }

    private SessionActorResponse toActorResponse(AuthenticatedActor actor) {
        return new SessionActorResponse(
                actor.actorId(),
                actor.displayName(),
                actor.role(),
                actor.mapMemberships().stream().sorted().toList());
    }

    private SessionActorResponse toActorResponse(SessionActorDefinition actor) {
        return new SessionActorResponse(
                actor.actorId(),
                actor.displayName(),
                actor.role(),
                actor.mapMemberships().stream().sorted().toList());
    }

    private void writeSessionCookie(HttpServletResponse response, String sessionId) {
        ResponseCookie cookie = ResponseCookie.from(SESSION_COOKIE_NAME, sessionId)
                .httpOnly(true)
                .sameSite("Lax")
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private record SessionActorDefinition(
            String actorId,
            String displayName,
            ActorRole role,
            Set<String> mapMemberships
    ) {
    }
}
