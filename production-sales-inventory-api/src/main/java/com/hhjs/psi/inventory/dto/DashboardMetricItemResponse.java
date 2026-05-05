package com.hhjs.psi.inventory.dto;

public record DashboardMetricItemResponse(
        String key,
        String title,
        String value,
        String subtitle,
        String icon,
        String accentColor,
        String change,
        String trendClass,
        String progress,
        String progressColor
) {
}
