package com.hhjs.psi.inventory.dto;

import java.time.LocalDate;

public record BusinessFlowTrendItemResponse(
        LocalDate date,
        String label,
        int totalCount,
        int purchaseInboundCount,
        int productionUsageCount,
        int finishedProductInboundCount,
        int salesOutboundCount,
        int productionLossCount
) {
}
