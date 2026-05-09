package io.simplehex.map.transport;

import io.simplehex.map.domain.ActorRole;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TerrainCommandRequest.class, name = "set_cell_terrain"),
        @JsonSubTypes.Type(value = CellVisibilityCommandRequest.class, name = "set_cell_visibility"),
        @JsonSubTypes.Type(value = FeatureVisibilityCommandRequest.class, name = "set_cell_feature_visibility")
})
public interface MapCommandRequest {
    String type();

    String operationId();

    ActorRole actorRole();

    CellRefDto cell();
}
