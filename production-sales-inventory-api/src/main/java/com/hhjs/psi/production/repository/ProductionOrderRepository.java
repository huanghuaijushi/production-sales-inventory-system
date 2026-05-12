package com.hhjs.psi.production.repository;

import com.hhjs.psi.production.entity.ProductionOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {

    @Query("""
            SELECT po FROM ProductionOrder po
            JOIN FETCH po.product
            ORDER BY po.createdAt DESC
            """)
    List<ProductionOrder> findAllWithProduct();

    @Query("""
            SELECT po FROM ProductionOrder po
            JOIN FETCH po.product
            WHERE po.id = :id
            """)
    Optional<ProductionOrder> findByIdWithProduct(Long id);

    @Query("""
            SELECT po.id FROM ProductionOrder po
            WHERE LOWER(po.orderNo) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(po.batchNo) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    List<Long> findIdsByOrderNoOrBatchNo(String query);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT po FROM ProductionOrder po
            JOIN FETCH po.product
            WHERE po.id = :id
            """)
    Optional<ProductionOrder> findByIdForUpdate(Long id);
}
