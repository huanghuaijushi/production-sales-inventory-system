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
        String salesValue
) {
}
