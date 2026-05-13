package com.hhjs.psi.sales.goods.repository;

import com.hhjs.psi.sales.goods.entity.SalesGoods;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SalesGoodsRepository extends JpaRepository<SalesGoods, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @EntityGraph(attributePaths = {"components", "components.product", "channelPrices", "channelPrices.channel", "skus", "skus.components", "skus.components.product"})
    Optional<SalesGoods> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"components", "components.product", "channelPrices", "channelPrices.channel", "skus", "skus.components", "skus.components.product"})
    Page<SalesGoods> findByEnabledTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"components", "components.product", "channelPrices", "channelPrices.channel", "skus", "skus.components", "skus.components.product"})
    List<SalesGoods> findByEnabledTrueOrderByCodeAsc();

    @EntityGraph(attributePaths = {"components", "components.product", "channelPrices", "channelPrices.channel", "skus", "skus.components", "skus.components.product"})
    @Query("""
            SELECT g FROM SalesGoods g
            WHERE g.enabled = true
              AND (LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(g.code) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(g.category, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(g.specification, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(g.unit) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<SalesGoods> searchEnabledGoods(String query, Pageable pageable);
}
