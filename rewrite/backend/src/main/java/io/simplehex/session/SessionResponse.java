package io.simplehex.session;

import java.util.List;

public record SessionResponse(
        SessionActorDto currentActor,
        List<SessionActorDto> availableActors
) {
}
