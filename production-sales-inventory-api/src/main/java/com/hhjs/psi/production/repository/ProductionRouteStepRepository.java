package com.hhjs.psi.production.repository;

import com.hhjs.psi.production.entity.ProductionRouteStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionRouteStepRepository extends JpaRepository<ProductionRouteStep, Long> {

    @Query("""
            SELECT step FROM ProductionRouteStep step
            JOIN FETCH step.product
            WHERE step.product.enabled = true
            ORDER BY step.product.code ASC, step.sortOrder ASC, step.id ASC
            """)
    List<ProductionRouteStep> findAllWithProduct();

    @Query("""
            SELECT step FROM ProductionRouteStep step
            WHERE step.product.id = :productId
              AND step.enabled = true
            ORDER BY step.sortOrder ASC, step.id ASC
            """)
    List<ProductionRouteStep> findEnabledByProductId(Long productId);

    boolean existsByProductIdAndStepCodeAndIdNot(Long productId, String stepCode, Long id);

    boolean existsByProductIdAndStepCode(Long productId, String stepCode);
}
