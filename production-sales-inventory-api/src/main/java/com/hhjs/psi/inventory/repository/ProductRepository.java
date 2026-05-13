package com.hhjs.psi.inventory.repository;

import com.hhjs.psi.inventory.entity.Product;
import com.hhjs.psi.inventory.entity.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<Product> findByTypeAndEnabledTrue(ProductType type);

    List<Product> findByTypeInAndEnabledTrue(java.util.Set<ProductType> types);

    List<Product> findByEnabledTrue();

    Page<Product> findByEnabledTrue(Pageable pageable);

    @Query("""
            SELECT p FROM Product p LEFT JOIN p.category pc
            WHERE p.enabled = true
              AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(p.code) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(pc.name, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(p.specification, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(p.unit) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<Product> searchEnabledProducts(String query, Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN p.category pc WHERE p.enabled = true AND " +
           "(p.type = :type OR :type IS NULL) AND " +
           "(pc.name = :category OR :category IS NULL)")
    List<Product> findByFilters(ProductType type, String category);
}
