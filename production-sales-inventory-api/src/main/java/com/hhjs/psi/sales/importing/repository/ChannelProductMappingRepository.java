package com.hhjs.psi.sales.importing.repository;

import com.hhjs.psi.sales.importing.entity.ChannelProductMapping;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChannelProductMappingRepository extends JpaRepository<ChannelProductMapping, Long> {

    @EntityGraph(attributePaths = {"channel", "salesSku"})
    List<ChannelProductMapping> findByChannelIdOrderByPriorityAscIdAsc(Long channelId);

    @EntityGraph(attributePaths = {"channel", "salesSku"})
    List<ChannelProductMapping> findByChannelIdAndEnabledTrueOrderByPriorityAscIdAsc(Long channelId);

    @EntityGraph(attributePaths = {"channel", "salesSku"})
    Optional<ChannelProductMapping> findByChannelIdAndExternalSkuCodeAndEnabledTrue(Long channelId, String externalSkuCode);
}
