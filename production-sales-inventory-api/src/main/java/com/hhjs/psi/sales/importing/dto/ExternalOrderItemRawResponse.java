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
        BigDecimal resolvedUnitPrice,
        BigDecimal resolvedSubtotal,
        String priceSource,
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
                resolveUnitPrice(item),
                resolveSubtotal(item),
                resolvePriceSource(item),
                item.getMatchedProduct() == null ? null : item.getMatchedProduct().getId(),
                item.getMatchedProductCode(),
                item.getMatchedProductName(),
                item.getMapping() == null ? null : item.getMapping().getId(),
                item.getConvertedQuantity(),
                item.getMatchStatus().name(),
                item.getMatchMessage()
        );
    }

    private static BigDecimal resolveUnitPrice(ExternalOrderItemRaw item) {
        if (item.getExternalUnitPrice() != null && item.getExternalUnitPrice().compareTo(BigDecimal.ZERO) > 0) {
            return item.getExternalUnitPrice();
        }
        if (item.getMapping() != null && item.getMapping().getDefaultUnitPrice() != null && item.getConvertedQuantity() != null && item.getConvertedQuantity() > 0) {
            return item.getMapping().getDefaultUnitPrice().divide(BigDecimal.valueOf(item.getConvertedQuantity()), 2, java.math.RoundingMode.HALF_UP);
        }
        if (item.getMatchedProduct() != null && item.getMatchedProduct().getSalePrice() != null) {
            return item.getMatchedProduct().getSalePrice();
        }
        return BigDecimal.ZERO;
    }

    private static BigDecimal resolveSubtotal(ExternalOrderItemRaw item) {
        BigDecimal unitPrice = resolveUnitPrice(item);
        return unitPrice.multiply(item.getExternalQuantity() == null ? BigDecimal.ZERO : item.getExternalQuantity());
    }

    private static String resolvePriceSource(ExternalOrderItemRaw item) {
        if (item.getExternalUnitPrice() != null && item.getExternalUnitPrice().compareTo(BigDecimal.ZERO) > 0) {
            return "IMPORTED";
        }
        if (item.getMapping() != null && item.getMapping().getDefaultUnitPrice() != null) {
            return "MAPPING_DEFAULT";
        }
        if (item.getMatchedProduct() != null && item.getMatchedProduct().getSalePrice() != null) {
            return "PRODUCT_SALE_PRICE";
        }
        return "NONE";
    }
}
