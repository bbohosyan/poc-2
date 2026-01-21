package com.sap.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import com.sap.repository.TableRowRepository;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    @Autowired
    private TableRowRepository repository;

    @Override
    public Health health() {
        try {
            long count = repository.count();
            return Health.up()
                    .withDetail("total_rows", count)
                    .withDetail("database", "H2")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
