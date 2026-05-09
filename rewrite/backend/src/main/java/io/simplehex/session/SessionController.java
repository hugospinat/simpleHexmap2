package io.simplehex.session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    private final DemoSessionService demoSessionService;

    public SessionController(DemoSessionService demoSessionService) {
        this.demoSessionService = demoSessionService;
    }

    @GetMapping
    public SessionResponse getSession(HttpServletRequest request, HttpServletResponse response) {
        return demoSessionService.getOrCreateSession(request, response);
    }

    @PostMapping("/actors/{actorId}")
    public SessionResponse activateActor(
            @PathVariable String actorId,
            HttpServletResponse response
    ) {
        return demoSessionService.activate(actorId, response);
    }
}
