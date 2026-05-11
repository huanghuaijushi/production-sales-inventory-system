package com.hhjs.psi.inventory.dto;

import com.hhjs.psi.inventory.entity.StockCheckResultType;

public record StockCheckOrderItemResponse(
        Long id,
        Long productId,
        String productCode,
        String productName,
        Long batchId,
        String batchNo,
        Integer systemQuantity,
        Integer actualQuantity,
        Integer differenceQuantity,
        StockCheckResultType resultType,
        Long stockRecordId,
        String remark
) {
}
