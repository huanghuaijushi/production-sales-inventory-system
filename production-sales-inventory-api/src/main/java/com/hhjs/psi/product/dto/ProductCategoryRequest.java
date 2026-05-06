package com.hhjs.psi.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductCategoryRequest(
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 64, message = "分类名称最多64个字符")
        String name,
        String type,
        Integer sortOrder,
        Boolean enabled,
        @Size(max = 255, message = "备注最多255个字符")
        String remark
) {
}
