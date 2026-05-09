package io.simplehex.session;

import io.simplehex.map.domain.ActorRole;
import java.util.Set;

public record AuthenticatedActor(
        String sessionId,
        String actorId,
        String displayName,
        ActorRole role,
        Set<String> mapMemberships
) {
    public boolean isMemberOf(String mapId) {
        return mapMemberships.contains(mapId);
    }
}
