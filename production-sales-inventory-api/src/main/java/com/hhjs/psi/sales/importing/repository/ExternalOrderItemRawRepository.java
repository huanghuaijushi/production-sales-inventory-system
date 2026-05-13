package com.hhjs.psi.sales.importing.repository;

import com.hhjs.psi.sales.importing.entity.ExternalOrderItemRaw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExternalOrderItemRawRepository extends JpaRepository<ExternalOrderItemRaw, Long> {

    @Query("SELECT i FROM ExternalOrderItemRaw i JOIN i.externalOrder e WHERE e.batch.id = :batchId ORDER BY i.id ASC")
    List<ExternalOrderItemRaw> findByOwnerBatchIdOrderByIdAsc(Long batchId);

    @Query("SELECT COUNT(i) FROM ExternalOrderItemRaw i JOIN i.externalOrder e WHERE e.batch.id = :batchId")
    int countByOwnerBatchId(Long batchId);

    @Query("SELECT COUNT(i) FROM ExternalOrderItemRaw i JOIN i.externalOrder e WHERE e.batch.id = :batchId AND i.matchedSalesSku IS NOT NULL")
    int countByOwnerBatchIdAndMatchedSalesSkuIsNotNull(Long batchId);

    @Query("SELECT COUNT(i) FROM ExternalOrderItemRaw i JOIN i.externalOrder e WHERE e.batch.id = :batchId AND i.matchedSalesSku IS NULL")
    int countByOwnerBatchIdAndMatchedSalesSkuIsNull(Long batchId);
}
