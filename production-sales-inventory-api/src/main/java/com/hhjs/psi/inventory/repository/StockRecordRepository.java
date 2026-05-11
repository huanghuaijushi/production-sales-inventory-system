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

    @Query("SELECT sr FROM StockRecord sr JOIN FETCH sr.product LEFT JOIN FETCH sr.batch ORDER BY sr.createdAt DESC")
    Page<StockRecord> findAllWithProduct(Pageable pageable);

    @Query("SELECT sr FROM StockRecord sr JOIN FETCH sr.product LEFT JOIN FETCH sr.batch WHERE sr.product.id = :productId ORDER BY sr.createdAt DESC")
    List<StockRecord> findByProductId(Long productId);

    @Query("SELECT sr FROM StockRecord sr JOIN FETCH sr.product LEFT JOIN FETCH sr.batch WHERE sr.type = :type ORDER BY sr.createdAt DESC")
    Page<StockRecord> findByType(StockRecordType type, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(sr.costAmount), 0)
            FROM StockRecord sr
            WHERE sr.relatedOrderType = :relatedOrderType
              AND sr.relatedOrderId = :relatedOrderId
              AND sr.type = :type
              AND sr.subType = :subType
            """)
    java.math.BigDecimal sumCostAmountByRelatedOrderAndTypeAndSubType(
            String relatedOrderType,
            Long relatedOrderId,
            StockRecordType type,
            StockRecordSubType subType
    );

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

    long countByTypeAndSubTypeInAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            StockRecordType type,
            List<StockRecordSubType> subTypes,
            Instant start,
            Instant end
    );

    @Query("""
            SELECT sr.createdAt, sr.type, sr.quantity, sr.subType, p.type, p.costPrice, sr.amount, sr.businessAmount, sr.costAmount
            FROM StockRecord sr
            JOIN sr.product p
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

    @Query("""
            SELECT COALESCE(SUM(sr.quantity), 0)
            FROM StockRecord sr
            WHERE sr.type = :type
              AND sr.subType IN :subTypes
              AND sr.createdAt >= :start
              AND sr.createdAt < :end
            """)
    Long sumQuantityByTypeAndSubTypes(
            StockRecordType type,
            List<StockRecordSubType> subTypes,
            Instant start,
            Instant end
    );

    @Query("""
            SELECT p.name, COALESCE(SUM(ABS(sr.quantity)), 0)
            FROM StockRecord sr
            JOIN sr.product p
            WHERE sr.type = com.hhjs.psi.inventory.entity.StockRecordType.OUT
              AND sr.subType = com.hhjs.psi.inventory.entity.StockRecordSubType.SALES
              AND p.type = com.hhjs.psi.inventory.entity.ProductType.FINISHED_PRODUCT
              AND sr.createdAt >= :start
              AND sr.createdAt < :end
            GROUP BY p.id, p.name
            ORDER BY COALESCE(SUM(ABS(sr.quantity)), 0) DESC, p.name ASC
            """)
    List<Object[]> findSalesRankingRows(Instant start, Instant end);

    @Query(value = "SELECT COUNT(*) FROM sales_order WHERE status = 'PENDING'", nativeQuery = true)
    long countPendingSalesOrders();

    @Query(value = """
            SELECT sc.name,
                   COALESCE(SUM(sr.business_amount), 0) AS revenue,
                   COALESCE(SUM(sr.cost_amount), 0) AS cost,
                   COUNT(DISTINCT so.id) AS order_count
            FROM stock_record sr
            LEFT JOIN sales_order so ON so.id = sr.related_order_id AND sr.related_order_type = 'SALES_ORDER'
            LEFT JOIN sales_channel_config sc ON sc.id = so.channel_id
            WHERE sr.type = 'OUT'
              AND sr.sub_type = 'SALES'
              AND sr.created_at >= :start
              AND sr.created_at < :end
            GROUP BY sc.id, sc.name
            ORDER BY revenue DESC, sc.name ASC
            """, nativeQuery = true)
    List<Object[]> findProfitChannelRows(Instant start, Instant end);
}
