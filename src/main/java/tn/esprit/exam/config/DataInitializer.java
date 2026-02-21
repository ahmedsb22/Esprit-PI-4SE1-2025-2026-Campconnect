package tn.esprit.exam.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Data is seeded via src/main/resources/data.sql (runs at startup via spring.sql.init.mode=always).
 * This class is kept as a no-op to avoid breaking any references.
 */
@Component
@Slf4j
public class DataInitializer {

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        log.info("DataInitializer: Data seeded via data.sql — nothing to do here.");
    }
}
