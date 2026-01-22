package com.sap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sap.event.RowCreatedEvent;
import com.sap.metrics.TableRowMetrics;
import com.sap.dto.CreateTableRowRequest;
import com.sap.entity.TableRow;
import com.sap.repository.TableRowRepository;

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
                request.getTypeNumber(), request.getTypeSelector());

        TableRow row = new TableRow();
        row.setTypeNumber(request.getTypeNumber());
        row.setTypeSelector(request.getTypeSelector());
        row.setTypeFreeText(sanitizationService.sanitize(request.getTypeFreeText()));

        TableRow saved = repository.save(row);

        eventPublisher.publishEvent(new RowCreatedEvent(this, saved));

        metrics.incrementCreated();

        LOG.info("Row created with ID: {}", saved.getId());
        return saved;
    }
}
