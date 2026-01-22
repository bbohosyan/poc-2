package com.sap.service;

import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sap.dto.CreateTableRowRequest;
import com.sap.entity.TableRow;
import com.sap.repository.TableRowRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class AsyncTableRowService {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncTableRowService.class);

    @Autowired
    private TableRowRepository repository;

    @Autowired
    private SanitizationService sanitizationService;

    @Autowired
    private BatchTableRowService batchService;

    @Async("taskExecutor")
    @Timed(value = "async.bulk.create", description = "Time to create bulk rows")
    public CompletableFuture<List<TableRow>> createBulk(List<CreateTableRowRequest> requests) {
        LOG.info("Starting bulk creation of {} rows", requests.size());

        List<TableRow> savedRows = new ArrayList<>();

        for (CreateTableRowRequest request : requests) {
            TableRow row = new TableRow();
            row.setTypeNumber(request.getTypeNumber());
            row.setTypeSelector(request.getTypeSelector());
            row.setTypeFreeText(sanitizationService.sanitize(request.getTypeFreeText()));

            TableRow saved = repository.save(row);
            savedRows.add(saved);
        }

        LOG.info("Completed bulk creation of {} rows", savedRows.size());
        return CompletableFuture.completedFuture(savedRows);
    }

    @Async("taskExecutor")
    @Transactional
    // TODO: make in seperate methods/ Async and Transactional
    public CompletableFuture<List<TableRow>> createBulkOptimized(List<CreateTableRowRequest> requests) {
        LOG.info("Starting optimized bulk creation of {} rows", requests.size());

        List<TableRow> rows = requests.stream()
                .map(request -> {
                    TableRow row = new TableRow();
                    row.setTypeNumber(request.getTypeNumber());
                    row.setTypeSelector(request.getTypeSelector());
                    row.setTypeFreeText(sanitizationService.sanitize(request.getTypeFreeText()));
                    return row;
                })
                .collect(Collectors.toList());

        List<TableRow> savedRows = batchService.saveBatch(rows);
        // if save in other DBs tables etc.

        LOG.info("Completed optimized bulk creation of {} rows", savedRows.size());
        return CompletableFuture.completedFuture(savedRows);
    }

    @Async("taskExecutor")
    public void generateReport(Long userId) {
        LOG.info("Generating report for user: {}", userId);

        try {
            Thread.sleep(5000);

            List<TableRow> allRows = repository.findAll();

            LOG.info("Report generated successfully for user: {}", userId);
        } catch (Exception e) {
            LOG.error("Failed to generate report for user: {}", userId, e);
        }
    }

    @Async("taskExecutor")
    public void generateMonthlyReport(Long userId) {
        LOG.info("Generating monthly report for user: {}", userId);
    }

    @Async("taskExecutor")
    public void exportToExcel(Long userId) {
        LOG.info("Exporting data to Excel for user: {}", userId);
    }
}
