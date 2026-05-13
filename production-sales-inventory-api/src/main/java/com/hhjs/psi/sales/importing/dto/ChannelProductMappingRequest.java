package com.hhjs.psi.sales.importing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChannelProductMappingRequest(
        @NotNull(message = "渠道不能为空")
        Long channelId,
        @NotBlank(message = "外部商品名称不能为空")
        @Size(max = 255, message = "外部商品名称最多255个字符")
        String externalProductName,
        @Size(max = 255, message = "外部规格名称最多255个字符")
        String externalSpecName,
        @Size(max = 120, message = "外部SKU编码最多120个字符")
        String externalSkuCode,
        @NotNull(message = "销售SKU不能为空")
        Long salesSkuId,
        @NotBlank(message = "匹配类型不能为空")
        String matchType,
        @NotNull(message = "是否启用不能为空")
        Boolean enabled,
        Integer priority,
        @Size(max = 500, message = "备注最多500个字符")
        String remark
) {
}
