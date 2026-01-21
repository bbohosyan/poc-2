package org.uniqa.repository;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.uniqa.entity.TableRow;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class TableRowRepositoryTest {

    @Inject
    TableRowRepository repository;

    @BeforeEach
    @Transactional
    void setup() {
        repository.deleteAll();
    }

    @Test
    @Transactional
    void Persist_ValidRow() {
        TableRow row = new TableRow();
        row.typeNumber = 42;
        row.typeSelector = "A";
        row.typeFreeText = "Test text";

        repository.persist(row);

        assertNotNull(row.id, "Row ID should be generated");
        assertTrue(row.id > 0, "Row ID should be positive");
    }

    @Test
    @Transactional
    void FindById_ExistingRow() {
        TableRow row = new TableRow();
        row.typeNumber = 10;
        row.typeSelector = "B";
        row.typeFreeText = "Find me";

        repository.persist(row);
        Long id = row.id;

        TableRow found = repository.findById(id);

        assertNotNull(found, "Should find the row");
        assertEquals(id, found.id, "IDs should match");
        assertEquals(10, found.typeNumber, "Type number should match");
        assertEquals("B", found.typeSelector, "Type selector should match");
        assertEquals("Find me", found.typeFreeText, "Type free text should match");
    }

    @Test
    void FindById_NonExistingRow() {
        TableRow found = repository.findById(99999L);
        assertNull(found, "Should not find non-existing row");
    }

    @Test
    @Transactional
    void FindAll_EmptyDatabase() {
        List<TableRow> rows = repository.findAll().list();
        assertTrue(rows.isEmpty(), "Empty database should return empty list");
    }

    @Test
    @Transactional
    void FindAll_WithMultipleRows() {
        for (int i = 1; i <= 5; i++) {
            TableRow row = new TableRow();
            row.typeNumber = i;
            row.typeSelector = "A";
            row.typeFreeText = "Row " + i;
            repository.persist(row);
        }

        List<TableRow> rows = repository.findAll().list();

        assertEquals(5, rows.size(), "Should find all 5 rows");
    }

    @Test
    @Transactional
    void Count_EmptyDatabase() {
        long count = repository.count();
        assertEquals(0, count, "Empty database should have count 0");
    }

    @Test
    @Transactional
    void Count_WithRows() {
        for (int i = 1; i <= 3; i++) {
            TableRow row = new TableRow();
            row.typeNumber = i;
            row.typeSelector = "C";
            row.typeFreeText = "Count test " + i;
            repository.persist(row);
        }

        long count = repository.count();
        assertEquals(3, count, "Should count 3 rows");
    }

    @Test
    @Transactional
    void DeleteById_ExistingRow() {
        TableRow row = new TableRow();
        row.typeNumber = 1;
        row.typeSelector = "A";
        row.typeFreeText = "To be deleted";

        repository.persist(row);
        Long id = row.id;

        boolean deleted = repository.deleteById(id);

        assertTrue(deleted, "Delete should return true for existing row");
        assertNull(repository.findById(id), "Row should no longer exist");
    }

    @Test
    @Transactional
    void DeleteById_NonExistingRow() {
        boolean deleted = repository.deleteById(99999L);
        assertFalse(deleted, "Delete should return false for non-existing row");
    }

    @Test
    @Transactional
    void DeleteAll() {
        for (int i = 1; i <= 5; i++) {
            TableRow row = new TableRow();
            row.typeNumber = i;
            row.typeSelector = "A";
            row.typeFreeText = "Row " + i;
            repository.persist(row);
        }

        assertEquals(5, repository.count(), "Should have 5 rows before delete");

        repository.deleteAll();

        assertEquals(0, repository.count(), "Should have 0 rows after deleteAll");
    }

    @Test
    @Transactional
    void Pagination_FirstPage() {
        for (int i = 1; i <= 15; i++) {
            TableRow row = new TableRow();
            row.typeNumber = i;
            row.typeSelector = "A";
            row.typeFreeText = "Row " + i;
            repository.persist(row);
        }

        List<TableRow> firstPage = repository.findAll().page(0, 10).list();

        assertEquals(10, firstPage.size(), "First page should have 10 items");
    }

    @Test
    @Transactional
    void Pagination_SecondPage() {
        for (int i = 1; i <= 15; i++) {
            TableRow row = new TableRow();
            row.typeNumber = i;
            row.typeSelector = "A";
            row.typeFreeText = "Row " + i;
            repository.persist(row);
        }

        List<TableRow> secondPage = repository.findAll().page(1, 10).list();

        assertEquals(5, secondPage.size(), "Second page should have 5 items");
    }

    @Test
    @Transactional
    void Pagination_EmptyPage() {
        for (int i = 1; i <= 5; i++) {
            TableRow row = new TableRow();
            row.typeNumber = i;
            row.typeSelector = "A";
            row.typeFreeText = "Row " + i;
            repository.persist(row);
        }

        List<TableRow> emptyPage = repository.findAll().page(5, 10).list();

        assertTrue(emptyPage.isEmpty(), "Page beyond data should be empty");
    }

    @Test
    @Transactional
    void Persist_WithMaxValues() {
        TableRow row = new TableRow();
        row.typeNumber = 2147483647;
        row.typeSelector = "C";
        row.typeFreeText = "x".repeat(1000);

        repository.persist(row);

        assertNotNull(row.id, "Row with max values should be persisted");

        TableRow found = repository.findById(row.id);
        assertEquals(2147483647, found.typeNumber, "Max type number should be preserved");
        assertEquals(1000, found.typeFreeText.length(), "Max text length should be preserved");
    }

    @Test
    @Transactional
    void Persist_WithMinValues() {
        TableRow row = new TableRow();
        row.typeNumber = 1;
        row.typeSelector = "A";
        row.typeFreeText = "a";

        repository.persist(row);

        assertNotNull(row.id, "Row with min values should be persisted");

        TableRow found = repository.findById(row.id);
        assertEquals(1, found.typeNumber, "Min type number should be preserved");
        assertEquals("a", found.typeFreeText, "Min text should be preserved");
    }

    @Test
    @Transactional
    void Persist_MultipleRowsWithSameData() {
        TableRow row1 = new TableRow();
        row1.typeNumber = 5;
        row1.typeSelector = "B";
        row1.typeFreeText = "Duplicate data";

        TableRow row2 = new TableRow();
        row2.typeNumber = 5;
        row2.typeSelector = "B";
        row2.typeFreeText = "Duplicate data";

        repository.persist(row1);
        repository.persist(row2);

        assertNotNull(row1.id, "First row should be persisted");
        assertNotNull(row2.id, "Second row should be persisted");
        assertNotEquals(row1.id, row2.id, "IDs should be different even with same data");

        assertEquals(2, repository.count(), "Should have 2 rows with duplicate data");
    }

    @Test
    @Transactional
    void Update_ExistingRow() {
        TableRow row = new TableRow();
        row.typeNumber = 10;
        row.typeSelector = "A";
        row.typeFreeText = "Original text";

        repository.persist(row);
        Long id = row.id;

        row.typeNumber = 20;
        row.typeSelector = "B";
        row.typeFreeText = "Updated text";

        repository.persist(row);

        TableRow updated = repository.findById(id);
        assertEquals(20, updated.typeNumber, "Type number should be updated");
        assertEquals("B", updated.typeSelector, "Type selector should be updated");
        assertEquals("Updated text", updated.typeFreeText, "Type free text should be updated");
    }
}
