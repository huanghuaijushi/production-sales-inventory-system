package com.hhjs.psi.sales.importing.repository;

import com.hhjs.psi.sales.importing.entity.ExternalOrderRaw;
import com.hhjs.psi.sales.importing.entity.ExternalOrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExternalOrderRawRepository extends JpaRepository<ExternalOrderRaw, Long> {

    boolean existsByChannelIdAndExternalOrderNo(Long channelId, String externalOrderNo);

    @EntityGraph(attributePaths = {"channel", "batch", "salesOrder", "items", "items.matchedSalesSku", "items.mapping"})
    List<ExternalOrderRaw> findByBatchIdOrderByIdAsc(Long batchId);

    @EntityGraph(attributePaths = {"channel", "batch", "salesOrder", "items", "items.matchedSalesSku", "items.mapping"})
    Optional<ExternalOrderRaw> findWithDetailsById(Long id);

    List<ExternalOrderRaw> findByBatchIdAndStatus(Long batchId, ExternalOrderStatus status);
}
