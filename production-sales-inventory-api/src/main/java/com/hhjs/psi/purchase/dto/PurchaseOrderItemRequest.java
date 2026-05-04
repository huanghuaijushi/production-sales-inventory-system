package com.hhjs.psi.purchase.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PurchaseOrderItemRequest(
        @NotNull(message = "商品不能为空")
        Long productId,

        @NotNull(message = "采购数量不能为空")
        @Min(value = 1, message = "采购数量必须大于0")
        Integer quantity,

        @NotNull(message = "采购单价不能为空")
        @DecimalMin(value = "0.00", message = "采购单价不能小于0")
        BigDecimal unitPrice
) {
}
