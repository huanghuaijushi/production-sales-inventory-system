package com.hhjs.psi.sales.importing.repository;

import com.hhjs.psi.sales.importing.entity.SalesChannelConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalesChannelConfigRepository extends JpaRepository<SalesChannelConfig, Long> {

    List<SalesChannelConfig> findAllByOrderBySortOrderAscIdAsc();

    List<SalesChannelConfig> findByEnabledTrueOrderBySortOrderAscIdAsc();

    Optional<SalesChannelConfig> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
