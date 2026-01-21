package org.uniqa.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniqa.event.RowCreatedEvent;
import org.uniqa.metrics.TableRowMetrics;
import org.uniqa.dto.CreateTableRowRequest;
import org.uniqa.entity.TableRow;
import org.uniqa.repository.TableRowRepository;

@Service
public class TableRowService {

    private static final Logger LOG = LoggerFactory.getLogger(TableRowService.class);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TableRowRepository repository;

    @Autowired
    private SanitizationService sanitizationService;

    @Autowired
    private TableRowMetrics metrics;

    @Transactional
    public TableRow create(CreateTableRowRequest request) {
        LOG.info("Creating row - typeNumber: {}, typeSelector: {}",
                request.typeNumber, request.typeSelector);

        TableRow row = new TableRow();
        row.typeNumber = request.typeNumber;
        row.typeSelector = request.typeSelector;
        row.typeFreeText = sanitizationService.sanitize(request.typeFreeText);

        TableRow saved = repository.save(row);

        // Publish event
        eventPublisher.publishEvent(new RowCreatedEvent(this, saved));

        // Track metrics
        metrics.incrementCreated();

        LOG.info("Row created with ID: {}", saved.id);
        return saved;
    }
}
