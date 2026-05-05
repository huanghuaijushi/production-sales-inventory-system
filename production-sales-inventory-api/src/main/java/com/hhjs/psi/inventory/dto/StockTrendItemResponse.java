package com.hhjs.psi.inventory.dto;

import java.time.LocalDate;

public record StockTrendItemResponse(
        LocalDate date,
        String label,
        int inboundQuantity,
        int outboundQuantity,
        int netChangeQuantity,
        int rawMaterialInboundQuantity,
        int rawMaterialOutboundQuantity,
        int finishedProductInboundQuantity,
        int finishedProductOutboundQuantity,
        int productionLossQuantity
) {
}
