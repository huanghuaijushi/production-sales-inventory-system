package com.hhjs.psi.inventory.dto;

public record TodayBusinessOverviewResponse(
        long inboundRecordCount,
        long outboundRecordCount,
        long lossRecordCount,
        long pendingSalesOrderCount,
        int inboundQuantity,
        int outboundQuantity,
        int lossQuantity
) {
}
