package com.hhjs.psi.inventory.repository;

import com.hhjs.psi.inventory.entity.StockBatch;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockBatchRepository extends JpaRepository<StockBatch, Long> {

    @Query("""
            SELECT sb FROM StockBatch sb
            JOIN FETCH sb.product
            WHERE sb.product.id = :productId
              AND sb.availableQuantity > 0
            ORDER BY CASE WHEN sb.productionDate IS NULL THEN 1 ELSE 0 END, sb.productionDate ASC, sb.id ASC
            """)
    List<StockBatch> findAvailableByProductId(Long productId);

    @Query(value = """
            SELECT sb FROM StockBatch sb
            JOIN FETCH sb.product
            WHERE (:availableOnly = false OR sb.availableQuantity > 0)
              AND (:expiringBefore IS NULL OR (sb.expiryDate IS NOT NULL AND sb.expiryDate <= :expiringBefore))
            ORDER BY CASE WHEN sb.expiryDate IS NULL THEN 1 ELSE 0 END, sb.expiryDate ASC, sb.id ASC
            """,
            countQuery = """
            SELECT COUNT(sb) FROM StockBatch sb
            WHERE (:availableOnly = false OR sb.availableQuantity > 0)
              AND (:expiringBefore IS NULL OR (sb.expiryDate IS NOT NULL AND sb.expiryDate <= :expiringBefore))
            """)
    Page<StockBatch> findAvailableBatchesByExpiry(
            @Param("availableOnly") boolean availableOnly,
            @Param("expiringBefore") LocalDate expiringBefore,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sb FROM StockBatch sb JOIN FETCH sb.product WHERE sb.id = :id")
    Optional<StockBatch> findByIdForUpdate(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT sb FROM StockBatch sb
            JOIN FETCH sb.product
            WHERE sb.product.id = :productId
              AND sb.batchNo = :batchNo
              AND (sb.productionDate = :productionDate OR (sb.productionDate IS NULL AND :productionDate IS NULL))
            """)
    Optional<StockBatch> findMatchingBatchForUpdate(Long productId, String batchNo, LocalDate productionDate);

    @Modifying
    @Query(value = """
            INSERT INTO stock_batch (
                product_id,
                batch_no,
                production_date,
                expiry_date,
                quantity,
                available_quantity,
                remark,
                created_at,
                updated_at
            )
            SELECT
                sr.product_id,
                sr.record_no,
                DATE(sr.created_at),
                sr.expiry_date,
                sr.quantity,
                sr.quantity,
                CONCAT('历史入库补批次: ', sr.record_no),
                sr.created_at,
                sr.created_at
            FROM stock_record sr
            LEFT JOIN stock_batch sb ON sb.batch_no = sr.record_no AND sb.product_id = sr.product_id
            WHERE sr.type = 'IN'
              AND sr.quantity > 0
              AND sr.batch_id IS NULL
              AND sb.id IS NULL
            """, nativeQuery = true)
    int backfillBatchesForInboundRecords();

    @Modifying
    @Query(value = """
            UPDATE stock_record sr
            JOIN stock_batch sb ON sb.batch_no = sr.record_no AND sb.product_id = sr.product_id
            SET sr.batch_id = sb.id,
                sr.batch_no = sb.batch_no,
                sr.production_date = sb.production_date,
                sr.expiry_date = sb.expiry_date
            WHERE sr.type = 'IN'
              AND sr.quantity > 0
              AND sr.batch_id IS NULL
            """, nativeQuery = true)
    int linkInboundRecordsToBackfilledBatches();
}
