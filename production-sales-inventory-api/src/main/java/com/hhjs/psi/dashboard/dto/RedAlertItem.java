package com.hhjs.psi.dashboard.dto;

public record RedAlertItem(
        Long productId,
        String productName,
        String productType,
        Integer current,
        Integer safety
) {
}
