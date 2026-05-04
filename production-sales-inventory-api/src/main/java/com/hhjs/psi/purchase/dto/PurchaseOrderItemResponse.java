package com.hhjs.psi.purchase.dto;

import java.math.BigDecimal;

public record PurchaseOrderItemResponse(
        Long id,
        Long productId,
        String productCode,
        String productName,
        String productSpecification,
        String productUnit,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal amount
) {
}
