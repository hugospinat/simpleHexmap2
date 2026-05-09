package io.simplehex.map.bootstrap;

import io.simplehex.map.persistence.MapPersistenceRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoMapBootstrap implements ApplicationRunner {

    private final MapPersistenceRepository mapRepository;

    public DemoMapBootstrap(MapPersistenceRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        mapRepository.seedDemoMapIfMissing();
    }
}
