package com.hhjs.psi.production.repository;

import com.hhjs.psi.production.entity.ProductionOrderStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionOrderStepRepository extends JpaRepository<ProductionOrderStep, Long> {

    List<ProductionOrderStep> findByProductionOrderIdOrderBySortOrderAscIdAsc(Long productionOrderId);

    Optional<ProductionOrderStep> findByProductionOrderIdAndStepCode(Long productionOrderId, String stepCode);
}
