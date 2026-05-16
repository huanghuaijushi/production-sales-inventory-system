package com.hhjs.psi.dashboard.dto;

public record YellowAlertItem(
        Long productId,
        String productName,
        String batchNo,
        Integer daysToExpiry
) {
}
