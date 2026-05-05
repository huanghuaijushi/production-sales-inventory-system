package com.hhjs.psi.inventory.dto;

public record DashboardRankingItemResponse(
        int rank,
        String name,
        String percent,
        String value
) {
}
