package com.hhjs.psi.production.dto;

import java.math.BigDecimal;

public record SupplierMaterialResponse(
        Long id,
        Long supplierId,
        String supplierName,
        Long productId,
        String productCode,
        String productName,
        String productUnit,
        BigDecimal defaultUnitPrice,
        Integer minOrderQuantity,
        Integer orderMultiple,
        Integer leadTimeDays,
        Boolean preferred,
        String remark
) {
}
