package com.hhjs.psi.production.dto;

import java.util.List;

public record ProductionOrderDetailResponse(
        ProductionOrderSummaryResponse order,
        List<ProductionMaterialPlanResponse> materialPlans,
        List<ProductionMaterialIssueResponse> materialIssues,
        List<ProductionStepRecordResponse> stepRecords
) {
}
