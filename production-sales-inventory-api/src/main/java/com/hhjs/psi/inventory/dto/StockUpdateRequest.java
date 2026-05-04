package com.hhjs.psi.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockUpdateRequest(
        @NotNull(message = "库存数量不能为空")
        @Min(value = 0, message = "库存数量不能小于0")
        Integer quantity,

        @NotNull(message = "警戒库存不能为空")
        @Min(value = 0, message = "警戒库存不能小于0")
        Integer alertQuantity,

        @Size(max = 500, message = "备注长度不能超过500个字符")
        String remark
) {
}
