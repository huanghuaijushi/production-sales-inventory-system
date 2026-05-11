package com.hhjs.psi.sales.importing.repository;

import com.hhjs.psi.sales.importing.entity.ChannelProductMapping;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelProductMappingRepository extends JpaRepository<ChannelProductMapping, Long> {

    @EntityGraph(attributePaths = {"channel", "salesGoods"})
    List<ChannelProductMapping> findByChannelIdOrderByPriorityAscIdAsc(Long channelId);

    @EntityGraph(attributePaths = {"channel", "salesGoods"})
    List<ChannelProductMapping> findByChannelIdAndEnabledTrueOrderByPriorityAscIdAsc(Long channelId);
}
