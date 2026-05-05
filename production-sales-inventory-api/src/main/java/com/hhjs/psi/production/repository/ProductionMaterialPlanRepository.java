package com.hhjs.psi.production.repository;

import com.hhjs.psi.production.entity.ProductionMaterialPlan;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionMaterialPlanRepository extends JpaRepository<ProductionMaterialPlan, Long> {

    @Query("""
            SELECT plan FROM ProductionMaterialPlan plan
            JOIN FETCH plan.productionOrder po
            JOIN FETCH plan.materialProduct mp
            WHERE po.id = :productionOrderId
            ORDER BY mp.code ASC
            """)
    List<ProductionMaterialPlan> findByProductionOrderIdWithProduct(Long productionOrderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT plan FROM ProductionMaterialPlan plan
            JOIN FETCH plan.productionOrder po
            JOIN FETCH plan.materialProduct mp
            WHERE plan.id = :id
            """)
    Optional<ProductionMaterialPlan> findByIdForUpdate(Long id);
}
