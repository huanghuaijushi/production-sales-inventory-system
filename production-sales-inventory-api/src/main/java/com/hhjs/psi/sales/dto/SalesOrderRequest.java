package com.hhjs.psi.sales.dto;

import com.hhjs.psi.sales.entity.SalesChannel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SalesOrderRequest(
        @NotNull(message = "销售渠道不能为空")
        SalesChannel channel,
        Long channelId,
        @Size(max = 120, message = "客户名称最多120个字符")
        String customerName,
        @Size(max = 20, message = "客户电话最多20个字符")
        String customerPhone,
        @Size(max = 255, message = "客户地址最多255个字符")
        String customerAddress,
        @Size(max = 500, message = "备注最多500个字符")
        String remark,
        @Valid
        @NotEmpty(message = "销售明细不能为空")
        List<SalesOrderItemRequest> items
) {
}
