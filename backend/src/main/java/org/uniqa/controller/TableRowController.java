package org.uniqa.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uniqa.dto.CreateTableRowRequest;
import org.uniqa.entity.TableRow;
import org.uniqa.repository.TableRowRepository;
import org.uniqa.service.SanitizationService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/rows")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost", "http://localhost:80"})
public class TableRowController {

    private static final Logger LOG = LoggerFactory.getLogger(TableRowController.class);

    @Autowired
    private TableRowRepository repository;

    @Autowired
    private SanitizationService sanitizationService;

    @GetMapping
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

        Page<TableRow> pageResult = repository.findAll(PageRequest.of(page, size));

        Map<String, Object> response = new HashMap<>();
        response.put("data", pageResult.getContent());
        response.put("totalCount", pageResult.getTotalElements());
        response.put("page", page);
        response.put("size", size);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TableRow> createRow(@Valid @RequestBody CreateTableRowRequest request) {
        LOG.info("Creating row - typeNumber: {}, typeSelector: {}",
                request.typeNumber, request.typeSelector);

        TableRow row = new TableRow();
        row.typeNumber = request.typeNumber;
        row.typeSelector = request.typeSelector;
        row.typeFreeText = sanitizationService.sanitize(request.typeFreeText);

        TableRow savedRow = repository.save(row);

        LOG.info("Row created with ID: {}", savedRow.id);
        return ResponseEntity.ok(savedRow);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRow(@PathVariable Long id) {
        LOG.info("Deleting row with ID: {}", id);
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
