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
        Long matchedSalesSkuId,
        String matchedSkuName,
        Long mappingId,
        Integer saleQuantity,
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
                item.getMatchedSalesSku() == null ? null : item.getMatchedSalesSku().getId(),
                item.getMatchedSkuName(),
                item.getMapping() == null ? null : item.getMapping().getId(),
                item.getSaleQuantity(),
                item.getMatchStatus().name(),
                item.getMatchMessage()
        );
    }

    private static BigDecimal resolveUnitPrice(ExternalOrderItemRaw item) {
        if (item.getExternalUnitPrice() != null && item.getExternalUnitPrice().compareTo(BigDecimal.ZERO) > 0) {
            return item.getExternalUnitPrice();
        }
        if (item.getMatchedSalesSku() != null && item.getMatchedSalesSku().getPerSkuPrice() != null) {
            return item.getMatchedSalesSku().getPerSkuPrice();
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
        if (item.getMatchedSalesSku() != null && item.getMatchedSalesSku().getPerSkuPrice() != null) {
            return "SKU_PRICE";
        }
        return "NONE";
    }
}
