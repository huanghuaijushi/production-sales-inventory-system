package com.hhjs.psi.production.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PurchaseSuggestionGroupResponse(
        Long supplierId,
        String supplierName,
        LocalDate expectedArrivalDate,
        BigDecimal totalAmount,
        List<PurchaseSuggestionItemResponse> items
) {
}
