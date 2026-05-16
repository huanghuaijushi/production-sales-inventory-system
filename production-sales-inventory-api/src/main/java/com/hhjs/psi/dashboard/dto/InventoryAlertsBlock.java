package com.hhjs.psi.dashboard.dto;

import java.util.List;

public record InventoryAlertsBlock(
        Integer redCount,
        Integer yellowCount,
        Integer greenCount,
        List<RedAlertItem> redAlerts,
        List<YellowAlertItem> yellowAlerts
) {
}
