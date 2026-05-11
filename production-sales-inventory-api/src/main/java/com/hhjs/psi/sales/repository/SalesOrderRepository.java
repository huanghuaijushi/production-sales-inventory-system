package com.hhjs.psi.sales.repository;

import com.hhjs.psi.sales.entity.SalesOrder;
import com.hhjs.psi.sales.entity.SalesOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    @EntityGraph(attributePaths = {"items", "items.salesGoods", "items.salesGoods.components", "items.salesGoods.components.product"})
    @Query("""
            SELECT so FROM SalesOrder so
            WHERE (:query IS NULL
                   OR LOWER(so.orderNo) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(so.customerName, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(so.customerPhone, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(so.remark, '')) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:status IS NULL OR so.status = :status)
            """)
    Page<SalesOrder> searchOrders(String query, SalesOrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.salesGoods", "items.salesGoods.components", "items.salesGoods.components.product"})
    Optional<SalesOrder> findWithDetailsById(Long id);
}
