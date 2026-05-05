package com.hhjs.psi.production.dto;

import java.time.Instant;

public record ProductionMaterialIssueResponse(
        Long id,
        Long materialPlanId,
        Long materialProductId,
        String materialProductCode,
        String materialProductName,
        String materialProductUnit,
        Long stockBatchId,
        String batchNo,
        Integer issuedQuantity,
        Long stockRecordId,
        String operatorName,
        String remark,
        Instant createdAt
) {
}
