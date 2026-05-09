package io.simplehex.session;

import io.simplehex.map.domain.ActorRole;
import java.util.List;

public record SessionActorResponse(
        String actorId,
        String displayName,
        ActorRole role,
        List<String> mapMemberships
) {
}
