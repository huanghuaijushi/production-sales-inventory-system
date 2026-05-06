package com.hhjs.psi.sales.importing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PddExcelImportRequest(
        @NotNull(message = "渠道不能为空")
        Long channelId,
        @NotBlank(message = "文件名不能为空")
        String fileName,
        @NotBlank(message = "Excel内容不能为空")
        String rawText
) {
}
