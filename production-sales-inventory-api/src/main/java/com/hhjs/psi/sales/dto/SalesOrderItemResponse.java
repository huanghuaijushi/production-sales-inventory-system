package com.hhjs.psi.sales.dto;

import java.math.BigDecimal;

public record SalesOrderItemResponse(
        Long id,
        Long salesGoodsId,
        String salesGoodsCode,
        String salesGoodsName,
        String salesGoodsSpecification,
        String salesGoodsUnit,
        String salesGoodsCategory,
        String externalProductName,
        String externalSpecName,
        BigDecimal externalQuantity,
        Long mappingId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
