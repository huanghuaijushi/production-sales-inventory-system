package com.hhjs.psi.production.dto;

import java.math.BigDecimal;

public record PurchaseSuggestionItemResponse(
        Long productId,
        String productCode,
        String productName,
        String productUnit,
        Integer shortageQuantity,
        Integer suggestedPurchaseQuantity,
        Integer availableQuantity,
        Integer onOrderQuantity,
        BigDecimal defaultUnitPrice,
        BigDecimal amount,
        Integer leadTimeDays,
        String reason
) {
}
