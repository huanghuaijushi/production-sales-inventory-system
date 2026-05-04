package com.hhjs.psi.inventory.dto;

import com.hhjs.psi.inventory.entity.ProductType;

import java.math.BigDecimal;

public record StockItemResponse(
        Long id,
        Long productId,
        String productCode,
        String productName,
        ProductType productType,
        String category,
        String specification,
        String unit,
        Integer quantity,
        Integer lockedQuantity,
        Integer availableQuantity,
        Integer alertQuantity,
        BigDecimal costPrice,
        BigDecimal salePrice,
        boolean isLowStock
) {
}
