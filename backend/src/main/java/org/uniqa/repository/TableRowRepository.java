package org.uniqa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.uniqa.entity.TableRow;

@Repository
public interface TableRowRepository extends JpaRepository<TableRow, Long> {
}
