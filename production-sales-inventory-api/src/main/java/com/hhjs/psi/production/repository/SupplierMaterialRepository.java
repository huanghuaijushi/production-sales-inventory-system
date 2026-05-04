package com.hhjs.psi.production.repository;

import com.hhjs.psi.production.entity.SupplierMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierMaterialRepository extends JpaRepository<SupplierMaterial, Long> {

    boolean existsBySupplierIdAndProductId(Long supplierId, Long productId);

    Optional<SupplierMaterial> findByIdAndSupplier_EnabledTrueAndProduct_EnabledTrue(Long id);

    @Query("""
            SELECT sm FROM SupplierMaterial sm
            JOIN FETCH sm.supplier s
            JOIN FETCH sm.product p
            WHERE s.enabled = true
              AND p.enabled = true
            ORDER BY p.code ASC, sm.preferred DESC, s.name ASC
            """)
    List<SupplierMaterial> findAllEnabledWithDetails();

    @Query("""
            SELECT sm FROM SupplierMaterial sm
            JOIN FETCH sm.supplier s
            JOIN FETCH sm.product p
            WHERE s.enabled = true
              AND p.enabled = true
              AND p.id IN :productIds
            ORDER BY p.code ASC, sm.preferred DESC, s.name ASC
            """)
    List<SupplierMaterial> findByProductIdInWithDetails(Collection<Long> productIds);
}
