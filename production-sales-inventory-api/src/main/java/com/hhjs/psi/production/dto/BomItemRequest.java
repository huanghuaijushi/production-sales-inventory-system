package com.hhjs.psi.production.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BomItemRequest(
        @NotNull(message = "成品不能为空")
        Long finishedProductId,

        @NotNull(message = "原材料不能为空")
        Long materialProductId,

        @NotNull(message = "用量不能为空")
        @DecimalMin(value = "0.0001", message = "用量必须大于0")
        BigDecimal quantityPerUnit,

        @DecimalMin(value = "0", message = "损耗率不能小于0")
        @DecimalMax(value = "1", message = "损耗率不能大于1")
        BigDecimal lossRate
) {
}
