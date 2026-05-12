package com.hhjs.psi.product.repository;

import com.hhjs.psi.inventory.entity.ProductType;
import com.hhjs.psi.product.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    boolean existsByNameAndType(String name, ProductType type);

    boolean existsByNameAndTypeAndIdNot(String name, ProductType type, Long id);

    Optional<ProductCategory> findByIdAndEnabledTrue(Long id);

    @Query("""
            SELECT pc FROM ProductCategory pc
            WHERE (:type IS NULL OR pc.type = :type OR pc.type IS NULL)
            ORDER BY pc.sortOrder ASC, pc.name ASC
            """)
    List<ProductCategory> findByOptionalType(ProductType type);

    @Query("""
            SELECT pc FROM ProductCategory pc
            WHERE pc.enabled = true
              AND (:type IS NULL OR pc.type = :type OR pc.type IS NULL)
            ORDER BY pc.sortOrder ASC, pc.name ASC
            """)
    List<ProductCategory> findEnabledByOptionalType(ProductType type);
}
