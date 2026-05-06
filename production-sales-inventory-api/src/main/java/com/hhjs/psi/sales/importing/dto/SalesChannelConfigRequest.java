package com.hhjs.psi.sales.importing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SalesChannelConfigRequest(
        @NotBlank(message = "渠道编码不能为空")
        @Size(max = 50, message = "渠道编码最多50个字符")
        String code,
        @NotBlank(message = "渠道名称不能为空")
        @Size(max = 80, message = "渠道名称最多80个字符")
        String name,
        @NotBlank(message = "来源类型不能为空")
        String sourceType,
        @NotNull(message = "是否启用不能为空")
        Boolean enabled,
        Integer sortOrder,
        String configJson,
        @Size(max = 500, message = "备注最多500个字符")
        String remark
) {
}
