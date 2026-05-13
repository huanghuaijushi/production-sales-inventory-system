package com.hhjs.psi.sales.goods.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record SalesSkuRequest(
        @NotBlank(message = "SKU编码不能为空")
        @Size(max = 64, message = "SKU编码最多64个字符")
        String code,
        @NotBlank(message = "SKU名称不能为空")
        @Size(max = 120, message = "SKU名称最多120个字符")
        String name,
        @Size(max = 64, message = "SKU规格最多64个字符")
        String specName,
        @NotBlank(message = "销售单位不能为空")
        @Size(max = 20, message = "销售单位最多20个字符")
        String unit,
        @DecimalMin(value = "0.00", message = "每SKU售价不能小于0")
        BigDecimal perSkuPrice,
        Boolean enabled,
        Long salesGoodsId,
        @Size(max = 500, message = "备注最多500个字符")
        String remark,
        @Valid
        List<ComponentRequest> components
) {
    public record ComponentRequest(
            @NotNull(message = "库存产品不能为空")
            Long productId,
            @NotNull(message = "组成数量不能为空")
            @DecimalMin(value = "0.0001", message = "组成数量必须大于0")
            BigDecimal quantity,
            @Size(max = 500, message = "备注最多500个字符")
            String remark
    ) {
    }
}
