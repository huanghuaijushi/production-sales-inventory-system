package com.hhjs.psi.inventory.dto;

import java.util.List;

public record ProfitOverviewResponse(
        String totalRevenue,
        String totalCost,
        String grossProfit,
        String grossMargin,
        String averageOrderRevenue,
        String averageOrderGrossProfit,
        List<DashboardMetricItemResponse> metrics,
        List<DashboardChartBarResponse> trendBars,
        List<DashboardRankingItemResponse> channelRanking,
        List<DashboardSummaryItemResponse> summaries
) {
}
