package com.sap.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sap.entity.TableRow;

import java.util.List;

@Service
public class BatchTableRowService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public List<TableRow> saveBatch(List<TableRow> rows) {
        int batchSize = 50;
        for (int i = 0; i < rows.size(); i++) {
            entityManager.persist(rows.get(i));
            if (i % batchSize == 0 && i > 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();
        return rows;
    }
}