package io.simplehex.map.transport;

import io.simplehex.map.application.MapService;
import io.simplehex.map.domain.ActorRole;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maps")
public class MapController {

    private final MapService mapService;

    public MapController(MapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping("/{mapId}")
    public MapSnapshotResponse getMapSnapshot(
            @PathVariable String mapId,
            @RequestParam(defaultValue = "gm") String role
    ) {
        return mapService.getSnapshot(mapId, ActorRole.fromValue(role));
    }

    @PostMapping("/{mapId}/commands")
    public CommandAppliedResponse applyCommand(
            @PathVariable String mapId,
            @Valid @RequestBody MapCommandRequest request
    ) {
        return mapService.applyCommand(mapId, request);
    }
}