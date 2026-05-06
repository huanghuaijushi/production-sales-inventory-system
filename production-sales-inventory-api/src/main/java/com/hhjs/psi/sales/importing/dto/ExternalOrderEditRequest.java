package com.hhjs.psi.sales.importing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ExternalOrderEditRequest(
        @Size(max = 120, message = "客户名称最多120个字符")
        String customerName,
        @Size(max = 30, message = "客户电话最多30个字符")
        String customerPhone,
        @Size(max = 500, message = "客户地址最多500个字符")
        String customerAddress,
        @Size(max = 500, message = "买家留言最多500个字符")
        String buyerMessage,
        @Size(max = 500, message = "卖家备注最多500个字符")
        String sellerRemark,
        @Valid
        @NotNull(message = "订单明细不能为空")
        List<Item> items
) {
    public record Item(
            Long id,
            @NotBlank(message = "外部商品名称不能为空")
            @Size(max = 255, message = "外部商品名称最多255个字符")
            String externalProductName,
            @Size(max = 255, message = "外部规格最多255个字符")
            String externalSpecName,
            @Size(max = 120, message = "外部SKU最多120个字符")
            String externalSkuCode,
            @NotNull(message = "外部数量不能为空")
            @DecimalMin(value = "0.0001", message = "外部数量必须大于0")
            BigDecimal externalQuantity,
            @NotNull(message = "成交价不能为空")
            @DecimalMin(value = "0.00", message = "成交价不能小于0")
            BigDecimal externalUnitPrice
    ) {
    }
}
