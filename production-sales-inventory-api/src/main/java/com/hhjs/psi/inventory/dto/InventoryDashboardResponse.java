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
        List<StockItemResponse> recentUpdates,
        List<DashboardMetricItemResponse> topMetrics,
        List<DashboardChartBarResponse> rawMaterialBars,
        List<DashboardChartBarResponse> finishedProductBars,
        List<DashboardWarningItemResponse> rawMaterialWarnings,
        List<DashboardRankingItemResponse> hotProducts,
        List<DashboardCategoryShareResponse> rawCategoryShares,
        List<DashboardCategoryShareResponse> finishedCategoryShares,
        List<DashboardSummaryItemResponse> rawSummaryCards,
        List<DashboardSummaryItemResponse> finishedSummaryCards
) {
}
