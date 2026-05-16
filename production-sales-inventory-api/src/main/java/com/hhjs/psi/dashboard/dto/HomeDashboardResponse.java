package com.hhjs.psi.dashboard.dto;

public record HomeDashboardResponse(
        SalesMonthBlock salesMonth,
        ReceivablesAndCashBlock receivablesAndCash,
        InventoryAlertsBlock inventoryAlerts,
        TodayTodosBlock todayTodos
) {
}
