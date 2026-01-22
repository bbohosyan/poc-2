package com.sap.controller;

import io.micrometer.core.instrument.Timer;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sap.service.AsyncTableRowService;
import com.sap.metrics.TableRowMetrics;
import com.sap.dto.CreateTableRowRequest;
import com.sap.entity.TableRow;
import com.sap.repository.TableRowRepository;
import com.sap.service.TableRowService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rows")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost", "http://localhost:80"})
public class TableRowController {

    private static final Logger LOG = LoggerFactory.getLogger(TableRowController.class);

    private static final int BULK_OPTIMIZATION_THRESHOLD = 50;

    @Autowired
    private TableRowRepository repository;

    @Autowired
    private AsyncTableRowService asyncTableRowService;

    @Autowired
    private TableRowService tableRowService;

    @Autowired
    private TableRowMetrics metrics;

    @GetMapping
    @Cacheable(value = "rows", key = "#page + '-' + #size")
    public ResponseEntity<?> getAllRows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        LOG.info("Getting rows - page: {}, size: {}", page, size);

        if (size > 100) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Size cannot exceed 100"));
        }
        if (size < 1) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Size must be at least 1"));
        }

        Timer.Sample sample = metrics.startTimer();

        Page<TableRow> pageResult = repository.findAll(PageRequest.of(page, size));

        metrics.recordTimer(sample);

        Map<String, Object> response = new HashMap<>();
        response.put("data", pageResult.getContent());
        response.put("totalCount", pageResult.getTotalElements());
        response.put("page", page);
        response.put("size", size);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @CacheEvict(value = {"rows", "rowCount"}, allEntries = true)
    public ResponseEntity<TableRow> createRow(@Valid @RequestBody CreateTableRowRequest request) {
        TableRow savedRow = tableRowService.create(request);
        return ResponseEntity.ok(savedRow);
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"rows", "rowCount"}, allEntries = true)
    public ResponseEntity<Void> deleteRow(@PathVariable Long id) {
        LOG.info("Deleting row with ID: {}", id);
        repository.deleteById(id);
        metrics.incrementDeleted();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    @CacheEvict(value = {"rows", "rowCount"}, allEntries = true)
    public ResponseEntity<Map<String, Object>> createBulk(
            @Valid @RequestBody List<CreateTableRowRequest> requests) {

        LOG.info("Bulk creation requested for {} rows", requests.size());

        String strategy;
        if (requests.size() >= BULK_OPTIMIZATION_THRESHOLD) {
            asyncTableRowService.createBulkOptimized(requests);
            strategy = "optimized_batch";
            LOG.info("Using optimized batch strategy for {} rows", requests.size());
        } else {
            asyncTableRowService.createBulk(requests);
            strategy = "sequential";
            LOG.info("Using sequential strategy for {} rows", requests.size());
        }

        return ResponseEntity.accepted()
                .body(Map.of(
                        "status", "processing",
                        "message", "Bulk creation started",
                        "count", requests.size(),
                        "strategy", strategy
                ));
    }

    @PostMapping("/reports/generate")
    public ResponseEntity<Map<String, Object>> generateReport(
            @RequestParam(defaultValue = "1") Long userId) {

        LOG.info("Report generation requested for user: {}", userId);

        asyncTableRowService.generateReport(userId);

        return ResponseEntity.accepted()
                .body(Map.of(
                        "status", "processing",
                        "message", "Report generation started",
                        "userId", userId
                ));
    }

    @PostMapping("/reports/monthly")
    public ResponseEntity<Map<String, Object>> generateMonthlyReport(
            @RequestParam(defaultValue = "1") Long userId) {

        LOG.info("Monthly report generation requested for user: {}", userId);

        asyncTableRowService.generateMonthlyReport(userId);

        return ResponseEntity.accepted()
                .body(Map.of(
                        "status", "processing",
                        "message", "Monthly report generation started",
                        "userId", userId
                ));
    }

    @PostMapping("/export/excel")
    public ResponseEntity<Map<String, Object>> exportToExcel(
            @RequestParam(defaultValue = "1") Long userId) {

        LOG.info("Excel export requested for user: {}", userId);

        asyncTableRowService.exportToExcel(userId);

        return ResponseEntity.accepted()
                .body(Map.of(
                        "status", "processing",
                        "message", "Excel export started",
                        "userId", userId
                ));
    }
}
