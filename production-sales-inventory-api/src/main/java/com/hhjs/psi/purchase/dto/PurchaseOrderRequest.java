package com.hhjs.psi.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record PurchaseOrderRequest(
        @NotNull(message = "供应商不能为空")
        Long supplierId,

        Boolean draft,

        LocalDate expectedArrivalDate,

        @Size(max = 500, message = "备注不能超过500个字符")
        String remark,

        @Valid
        @NotEmpty(message = "采购明细不能为空")
        List<PurchaseOrderItemRequest> items
) {
}
