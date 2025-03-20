package com.emerald.fda.records.api.repository;

import com.emerald.fda.records.api.entity.DrugApplicationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing {@link DrugApplicationRecord} entities.
 */
public interface DrugApplicationRecordRepository extends JpaRepository<DrugApplicationRecord, String> {
    /**
     * Finds all drug applications with pagination.
     */
    @Override
    Page<DrugApplicationRecord> findAll(Pageable pageable);
}
