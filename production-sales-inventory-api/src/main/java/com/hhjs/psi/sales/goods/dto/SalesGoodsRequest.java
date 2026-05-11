package com.hhjs.psi.sales.goods.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record SalesGoodsRequest(
        @NotBlank(message = "商品编码不能为空")
        @Size(max = 64, message = "商品编码最多64个字符")
        String code,
        @NotBlank(message = "商品名称不能为空")
        @Size(max = 120, message = "商品名称最多120个字符")
        String name,
        @Size(max = 64, message = "商品分类最多64个字符")
        String category,
        @Size(max = 64, message = "商品规格最多64个字符")
        String specification,
        @NotBlank(message = "销售单位不能为空")
        @Size(max = 20, message = "销售单位最多20个字符")
        String unit,
        @DecimalMin(value = "0.00", message = "默认售价不能小于0")
        BigDecimal defaultPrice,
        Boolean enabled,
        @Size(max = 500, message = "备注最多500个字符")
        String remark,
        @Valid
        @NotEmpty(message = "商品组成不能为空")
        List<ComponentRequest> components,
        @Valid
        List<ChannelPriceRequest> channelPrices
) {
    public record ComponentRequest(
            @NotNull(message = "库存产品不能为空")
            Long productId,
            @NotNull(message = "组成数量不能为空")
            @DecimalMin(value = "0.0001", message = "组成数量必须大于0")
            BigDecimal quantityPerUnit,
            @DecimalMin(value = "0.0000", message = "损耗率不能小于0")
            BigDecimal lossRate,
            @Size(max = 500, message = "备注最多500个字符")
            String remark
    ) {
    }

    public record ChannelPriceRequest(
            @NotNull(message = "渠道不能为空")
            Long channelId,
            @NotNull(message = "渠道价格不能为空")
            @DecimalMin(value = "0.00", message = "渠道价格不能小于0")
            BigDecimal price,
            Boolean enabled,
            @Size(max = 500, message = "备注最多500个字符")
            String remark
    ) {
    }
}
