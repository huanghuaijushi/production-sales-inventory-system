package com.hhjs.psi.inventory.dto;

public record DashboardWarningItemResponse(
        String name,
        String current,
        String safety,
        String days,
        String status,
        String statusClass
) {
}
