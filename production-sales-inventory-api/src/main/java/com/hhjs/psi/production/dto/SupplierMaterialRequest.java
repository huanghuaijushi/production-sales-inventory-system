package com.hhjs.psi.production.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SupplierMaterialRequest(
        @NotNull(message = "供应商不能为空")
        Long supplierId,

        @NotNull(message = "原材料不能为空")
        Long productId,

        @DecimalMin(value = "0", message = "默认单价不能小于0")
        BigDecimal defaultUnitPrice,

        @Min(value = 1, message = "最小起订量必须大于0")
        Integer minOrderQuantity,

        @Min(value = 1, message = "采购倍数必须大于0")
        Integer orderMultiple,

        @Min(value = 0, message = "交期不能小于0")
        Integer leadTimeDays,

        Boolean preferred,
        String remark
) {
}
