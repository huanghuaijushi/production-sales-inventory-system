package com.hhjs.psi.sales.goods.repository;

import com.hhjs.psi.sales.goods.entity.SalesSku;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalesSkuRepository extends JpaRepository<SalesSku, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @EntityGraph(attributePaths = {"components", "components.product"})
    Optional<SalesSku> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"components", "components.product"})
    List<SalesSku> findByEnabledTrueOrderByCodeAsc();

    @EntityGraph(attributePaths = {"components", "components.product"})
    List<SalesSku> findByEnabledTrue();

    @EntityGraph(attributePaths = {"components", "components.product"})
    List<SalesSku> findBySalesGoodsIdAndEnabledTrue(Long salesGoodsId);
}
