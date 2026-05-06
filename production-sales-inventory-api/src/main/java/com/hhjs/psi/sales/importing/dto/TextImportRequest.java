package com.hhjs.psi.sales.importing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TextImportRequest(
        @NotNull(message = "渠道不能为空")
        Long channelId,
        @NotBlank(message = "导入文本不能为空")
        String rawText
) {
}
