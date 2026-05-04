package com.hhjs.psi.production.repository;

import com.hhjs.psi.production.entity.BomItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BomItemRepository extends JpaRepository<BomItem, Long> {

    boolean existsByFinishedProductIdAndMaterialProductId(Long finishedProductId, Long materialProductId);

    boolean existsByFinishedProductIdAndMaterialProductIdAndIdNot(
            Long finishedProductId,
            Long materialProductId,
            Long id
    );

    Optional<BomItem> findByIdAndFinishedProduct_EnabledTrueAndMaterialProduct_EnabledTrue(Long id);

    @Query("""
            SELECT item FROM BomItem item
            JOIN FETCH item.finishedProduct fp
            JOIN FETCH item.materialProduct mp
            WHERE fp.enabled = true
              AND mp.enabled = true
            ORDER BY fp.code ASC, mp.code ASC
            """)
    List<BomItem> findAllEnabledWithProducts();

    @Query("""
            SELECT item FROM BomItem item
            JOIN FETCH item.finishedProduct fp
            JOIN FETCH item.materialProduct mp
            WHERE fp.enabled = true
              AND mp.enabled = true
              AND fp.id = :finishedProductId
            ORDER BY mp.code ASC
            """)
    List<BomItem> findByFinishedProductIdWithProducts(Long finishedProductId);
}
