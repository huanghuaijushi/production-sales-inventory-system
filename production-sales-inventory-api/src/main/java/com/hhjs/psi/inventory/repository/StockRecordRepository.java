package com.hhjs.psi.inventory.repository;

import com.hhjs.psi.inventory.entity.StockRecord;
import com.hhjs.psi.inventory.entity.StockRecordSubType;
import com.hhjs.psi.inventory.entity.StockRecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface StockRecordRepository extends JpaRepository<StockRecord, Long> {

    @Query("SELECT sr FROM StockRecord sr JOIN FETCH sr.product ORDER BY sr.createdAt DESC")
    Page<StockRecord> findAllWithProduct(Pageable pageable);

    @Query("SELECT sr FROM StockRecord sr JOIN FETCH sr.product WHERE sr.product.id = :productId ORDER BY sr.createdAt DESC")
    List<StockRecord> findByProductId(Long productId);

    @Query("SELECT sr FROM StockRecord sr JOIN FETCH sr.product WHERE sr.type = :type ORDER BY sr.createdAt DESC")
    Page<StockRecord> findByType(StockRecordType type, Pageable pageable);

    long countByTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            StockRecordType type,
            Instant start,
            Instant end
    );

    long countByTypeAndSubTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            StockRecordType type,
            StockRecordSubType subType,
            Instant start,
            Instant end
    );

    @Query("""
            SELECT sr.createdAt, sr.type, sr.quantity
            FROM StockRecord sr
            WHERE sr.createdAt >= :start
              AND sr.createdAt < :end
            """)
    List<Object[]> findTrendRows(Instant start, Instant end);

    @Query("""
            SELECT COALESCE(SUM(sr.quantity), 0)
            FROM StockRecord sr
            WHERE sr.type = :type
              AND sr.createdAt >= :start
              AND sr.createdAt < :end
            """)
    Long sumQuantityByType(StockRecordType type, Instant start, Instant end);

    @Query("""
            SELECT COALESCE(SUM(sr.quantity), 0)
            FROM StockRecord sr
            WHERE sr.type = :type
              AND sr.subType = :subType
              AND sr.createdAt >= :start
              AND sr.createdAt < :end
            """)
    Long sumQuantityByTypeAndSubType(
            StockRecordType type,
            StockRecordSubType subType,
            Instant start,
            Instant end
    );

    @Query(value = "SELECT COUNT(*) FROM sales_order WHERE status = 'PENDING'", nativeQuery = true)
    long countPendingSalesOrders();
}
