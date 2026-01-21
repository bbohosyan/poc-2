package com.sap.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class RowEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(RowEventListener.class);

    @EventListener
    @Async
    public void handleRowCreated(RowCreatedEvent event) {
        // Send notification, update search index, trigger workflow, etc.
        LOG.info("Row created event: {}", event.getRow().getId());
    }

    @EventListener
    @Async
    public void auditRowCreation(RowCreatedEvent event) {
        // Audit trail
        LOG.info("Audit: Row with ID {} was created", event.getRow().getId());
    }
}
