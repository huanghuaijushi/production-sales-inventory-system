package com.hhjs.psi.sales.dto;

import com.hhjs.psi.sales.entity.SalesOrderItem;

import java.math.BigDecimal;
import java.util.Comparator;

public record SalesOrderItemResponse(
        Long id,
        Long salesSkuId,
        String salesSkuName,
        String salesSkuSpecName,
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
    public static SalesOrderItemResponse from(SalesOrderItem item) {
        return new SalesOrderItemResponse(
                item.getId(),
                item.getSalesSku().getId(),
                item.getSkuName(),
                item.getSkuSpecName(),
                item.getSalesGoods() != null ? item.getSalesGoods().getId() : null,
                item.getGoodsCode(),
                item.getGoodsName(),
                item.getGoodsSpecification(),
                item.getGoodsUnit(),
                item.getGoodsCategory(),
                item.getExternalProductName(),
                item.getExternalSpecName(),
                item.getExternalQuantity(),
                item.getMapping() != null ? item.getMapping().getId() : null,
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}
