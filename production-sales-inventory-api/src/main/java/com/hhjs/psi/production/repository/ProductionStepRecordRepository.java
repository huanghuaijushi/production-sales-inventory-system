package com.hhjs.psi.production.repository;

import com.hhjs.psi.production.entity.ProductionStepRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ProductionStepRecordRepository extends JpaRepository<ProductionStepRecord, Long> {

    @Query("""
            SELECT record FROM ProductionStepRecord record
            JOIN FETCH record.productionOrder po
            WHERE po.id = :productionOrderId
            ORDER BY record.createdAt ASC, record.id ASC
            """)
    List<ProductionStepRecord> findByProductionOrderId(Long productionOrderId);

    @Query("""
            SELECT COUNT(record)
            FROM ProductionStepRecord record
            WHERE record.createdAt >= :start
              AND record.createdAt < :end
              AND record.lossQuantity > 0
            """)
    long countLossRecords(Instant start, Instant end);

    @Query("""
            SELECT COALESCE(SUM(record.lossQuantity), 0)
            FROM ProductionStepRecord record
            WHERE record.createdAt >= :start
              AND record.createdAt < :end
            """)
    Long sumLossQuantity(Instant start, Instant end);

    @Query("""
            SELECT record.createdAt, record.lossQuantity
            FROM ProductionStepRecord record
            WHERE record.createdAt >= :start
              AND record.createdAt < :end
              AND record.lossQuantity > 0
            """)
    List<Object[]> findLossTrendRows(Instant start, Instant end);
}
