package com.hhjs.psi.production.repository;

import com.hhjs.psi.production.entity.ProductionMaterialIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionMaterialIssueRepository extends JpaRepository<ProductionMaterialIssue, Long> {

    @Query("""
            SELECT issue FROM ProductionMaterialIssue issue
            JOIN FETCH issue.productionOrder po
            JOIN FETCH issue.materialPlan plan
            JOIN FETCH issue.materialProduct mp
            WHERE po.id = :productionOrderId
            ORDER BY issue.createdAt ASC, issue.id ASC
            """)
    List<ProductionMaterialIssue> findByProductionOrderIdWithDetails(Long productionOrderId);
}
