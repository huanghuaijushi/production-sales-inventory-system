package com.hhjs.psi.inventory.dto;

public record DashboardCategoryShareResponse(
        String name,
        String percent,
        String value,
        String ratio,
        String color,
        Integer productCount,
        Integer quantity
) {
}
