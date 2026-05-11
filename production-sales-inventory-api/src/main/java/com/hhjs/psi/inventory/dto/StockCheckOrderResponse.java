package com.hhjs.psi.inventory.dto;

import com.hhjs.psi.inventory.entity.StockCheckOrderStatus;

import java.time.Instant;
import java.util.List;

public record StockCheckOrderResponse(
        Long id,
        String checkNo,
        StockCheckOrderStatus status,
        String operatorName,
        String remark,
        Instant confirmedAt,
        Instant createdAt,
        List<StockCheckOrderItemResponse> items
) {
}
