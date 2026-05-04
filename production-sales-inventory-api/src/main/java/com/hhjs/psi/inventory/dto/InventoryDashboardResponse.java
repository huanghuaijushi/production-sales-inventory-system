package com.hhjs.psi.inventory.dto;

import java.util.List;

public record InventoryDashboardResponse(
        int totalProducts,
        int lowStockCount,
        int outOfStockCount,
        int availableStockQuantity,
        TodayBusinessOverviewResponse todayBusinessOverview,
        List<InventoryDistributionItemResponse> inventoryDistribution,
        List<StockItemResponse> lowStockItems,
        List<StockItemResponse> recentUpdates
) {
}
