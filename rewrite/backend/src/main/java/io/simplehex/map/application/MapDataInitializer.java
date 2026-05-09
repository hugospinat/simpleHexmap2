package io.simplehex.map.application;

import io.simplehex.map.persistence.JpaMapRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MapDataInitializer implements ApplicationRunner {

    private final JpaMapRepository mapRepository;

    public MapDataInitializer(JpaMapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        mapRepository.seedDemoMapIfMissing();
    }
}