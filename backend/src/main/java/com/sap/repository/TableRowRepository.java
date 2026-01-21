package com.sap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sap.entity.TableRow;

@Repository
public interface TableRowRepository extends JpaRepository<TableRow, Long> {
}
