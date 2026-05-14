package com.hhjs.psi.inventory.dto;

import java.util.List;

public record MobileHomeResponse(
        TodayBusinessOverviewResponse today,
        int totalProducts,
        int lowStockCount,
        int outOfStockCount,
        List<StockItemResponse> lowStockItems,
        List<DashboardWarningItemResponse> rawMaterialWarnings,
        List<ExpiringBatchItem> expiringBatches
) {
}
