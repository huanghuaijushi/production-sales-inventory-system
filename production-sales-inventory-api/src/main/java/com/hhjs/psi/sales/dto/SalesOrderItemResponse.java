package com.hhjs.psi.sales.dto;

import java.math.BigDecimal;

public record SalesOrderItemResponse(
        Long id,
        Long productId,
        String productCode,
        String productName,
        String productSpecification,
        String productUnit,
        String productCategory,
        String externalProductName,
        String externalSpecName,
        BigDecimal externalQuantity,
        Long mappingId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
