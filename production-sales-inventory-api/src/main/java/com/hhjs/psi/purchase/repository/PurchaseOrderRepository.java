package com.hhjs.psi.purchase.repository;

import com.hhjs.psi.purchase.entity.PurchaseOrder;
import com.hhjs.psi.purchase.entity.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query(value = """
            SELECT po FROM PurchaseOrder po
            JOIN FETCH po.supplier s
            WHERE (:status IS NULL OR po.status = :status)
              AND (:query IS NULL
                   OR LOWER(po.orderNo) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(po.remark, '')) LIKE LOWER(CONCAT('%', :query, '%')))
            """,
            countQuery = """
            SELECT COUNT(po) FROM PurchaseOrder po
            JOIN po.supplier s
            WHERE (:status IS NULL OR po.status = :status)
              AND (:query IS NULL
                   OR LOWER(po.orderNo) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(po.remark, '')) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<PurchaseOrder> searchOrders(String query, PurchaseOrderStatus status, Pageable pageable);

    @Query("""
            SELECT DISTINCT po FROM PurchaseOrder po
            JOIN FETCH po.supplier
            LEFT JOIN FETCH po.items item
            LEFT JOIN FETCH item.product
            WHERE po.id = :id
            """)
    Optional<PurchaseOrder> findWithDetailsById(Long id);

    @Query("""
            SELECT item.product.id, COALESCE(SUM(item.quantity), 0)
            FROM PurchaseOrder po
            JOIN po.items item
            WHERE po.status = :status
            GROUP BY item.product.id
            """)
    List<Object[]> sumItemQuantitiesByStatus(PurchaseOrderStatus status);

    long countByStatus(PurchaseOrderStatus status);
}
