package io.simplehex.map.transport;

import io.simplehex.map.application.MapService;
import io.simplehex.session.DemoSessionService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maps")
public class MapController {

    private final MapService mapService;
    private final DemoSessionService demoSessionService;

    public MapController(MapService mapService, DemoSessionService demoSessionService) {
        this.mapService = mapService;
        this.demoSessionService = demoSessionService;
    }

    @GetMapping("/{mapId}")
    public MapSnapshotResponse getMapSnapshot(
            @PathVariable String mapId,
            HttpServletRequest request
    ) {
        return mapService.getSnapshot(mapId, demoSessionService.requireActor(request, mapId));
    }

    @PostMapping("/{mapId}/commands")
    public CommandAppliedResponse applyCommand(
            @PathVariable String mapId,
            HttpServletRequest httpRequest,
            @Valid @RequestBody MapCommandRequest commandRequest
    ) {
        return mapService.applyCommand(mapId, commandRequest, demoSessionService.requireActor(httpRequest, mapId));
    }
}
