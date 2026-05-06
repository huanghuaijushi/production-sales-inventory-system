package com.hhjs.psi.sales.importing.dto;

import com.hhjs.psi.sales.importing.entity.ExternalOrderItemRaw;

import java.math.BigDecimal;

public record ExternalOrderItemRawResponse(
        Long id,
        String externalProductName,
        String externalSpecName,
        String externalSkuCode,
        BigDecimal externalQuantity,
        BigDecimal externalUnitPrice,
        Long matchedProductId,
        String matchedProductCode,
        String matchedProductName,
        Long mappingId,
        Integer convertedQuantity,
        String matchStatus,
        String matchMessage
) {
    public static ExternalOrderItemRawResponse from(ExternalOrderItemRaw item) {
        return new ExternalOrderItemRawResponse(
                item.getId(),
                item.getExternalProductName(),
                item.getExternalSpecName(),
                item.getExternalSkuCode(),
                item.getExternalQuantity(),
                item.getExternalUnitPrice(),
                item.getMatchedProduct() == null ? null : item.getMatchedProduct().getId(),
                item.getMatchedProductCode(),
                item.getMatchedProductName(),
                item.getMapping() == null ? null : item.getMapping().getId(),
                item.getConvertedQuantity(),
                item.getMatchStatus().name(),
                item.getMatchMessage()
        );
    }
}
