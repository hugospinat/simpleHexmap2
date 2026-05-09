package io.simplehex.session;

import java.util.List;

public record SessionResponse(
        SessionActorResponse currentActor,
        List<SessionActorResponse> availableActors
) {
}
