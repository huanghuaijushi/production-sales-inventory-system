package com.hhjs.psi.supplier.repository;

import com.hhjs.psi.supplier.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Optional<Supplier> findByIdAndEnabledTrue(Long id);

    Page<Supplier> findByEnabledTrue(Pageable pageable);

    @Query("""
            SELECT s FROM Supplier s
            WHERE s.enabled = true
              AND (:query IS NULL
                   OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(s.contactName, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(s.phone, '')) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<Supplier> searchEnabledSuppliers(String query, Pageable pageable);
}
