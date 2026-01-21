package com.sap.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class TableRowMetrics {
    private final Counter rowsCreated;
    private final Counter rowsDeleted;
    private final Timer queryTimer;

    public TableRowMetrics(MeterRegistry registry) {
        this.rowsCreated = Counter.builder("table.rows.created")
                .description("Total rows created")
                .register(registry);

        this.rowsDeleted = Counter.builder("table.rows.deleted")
                .description("Total rows deleted")
                .register(registry);

        this.queryTimer = Timer.builder("table.rows.query.time")
                .description("Time to query rows")
                .register(registry);
    }

    public void incrementCreated() { rowsCreated.increment(); }
    public void incrementDeleted() { rowsDeleted.increment(); }
    public Timer.Sample startTimer() { return Timer.start(); }
    public void recordTimer(Timer.Sample sample) { sample.stop(queryTimer); }
}
