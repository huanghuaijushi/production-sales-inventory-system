package com.hhjs.psi.inventory.dto;

public record DashboardChartBarResponse(
        String label,
        String inbound,
        String outbound,
        String stock,
        String production,
        String sales,
        String inboundValue,
        String outboundValue,
        String stockValue,
        String productionValue,
        String salesValue,
        String inboundQuantityValue,
        String outboundQuantityValue,
        String stockQuantityValue,
        String productionAmountValue,
        String salesAmountValue,
        String stockAmountValue
) {
}
