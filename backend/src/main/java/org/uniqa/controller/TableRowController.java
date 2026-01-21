package org.uniqa.controller;

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
import org.uniqa.service.AsyncTableRowService;
import org.uniqa.metrics.TableRowMetrics;
import org.uniqa.dto.CreateTableRowRequest;
import org.uniqa.entity.TableRow;
import org.uniqa.repository.TableRowRepository;
import org.uniqa.service.TableRowService;

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

        // Track query performance with timer
        Timer.Sample sample = metrics.startTimer();

        Page<TableRow> pageResult = repository.findAll(PageRequest.of(page, size));

        // Record query duration
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

    /**
     * Bulk creation endpoint - automatically chooses optimal strategy based on batch size.
     * For small batches (< 50): uses simple sequential save
     * For large batches (>= 50): uses optimized batch processing with EntityManager
     */
    @PostMapping("/bulk")
    @CacheEvict(value = {"rows", "rowCount"}, allEntries = true)
    public ResponseEntity<Map<String, Object>> createBulk(
            @Valid @RequestBody List<CreateTableRowRequest> requests) {

        LOG.info("Bulk creation requested for {} rows", requests.size());

        String strategy;
        if (requests.size() >= BULK_OPTIMIZATION_THRESHOLD) {
            // Use optimized batch processing for large datasets
            asyncTableRowService.createBulkOptimized(requests);
            strategy = "optimized_batch";
            LOG.info("Using optimized batch strategy for {} rows", requests.size());
        } else {
            // Use simple sequential save for small datasets
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

    /**
     * Generate report asynchronously - heavy operation that runs in background
     */
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

    /**
     * Generate monthly report asynchronously
     */
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

    /**
     * Export data to Excel asynchronously - heavy operation
     */
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
