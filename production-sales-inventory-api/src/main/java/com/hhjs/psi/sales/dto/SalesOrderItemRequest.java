package com.hhjs.psi.sales.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SalesOrderItemRequest(
        @NotNull(message = "商品不能为空")
        Long productId,
        @NotNull(message = "数量不能为空")
        @Positive(message = "数量必须大于0")
        Integer quantity,
        @NotNull(message = "销售单价不能为空")
        @DecimalMin(value = "0.00", message = "销售单价不能小于0")
        BigDecimal unitPrice
) {
}
