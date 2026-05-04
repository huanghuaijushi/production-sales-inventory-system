package com.hhjs.psi.supplier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierRequest(
        @NotBlank(message = "供应商名称不能为空")
        @Size(max = 120, message = "供应商名称不能超过120个字符")
        String name,

        @Size(max = 80, message = "联系人不能超过80个字符")
        String contactName,

        @Size(max = 30, message = "电话不能超过30个字符")
        String phone,

        @Size(max = 255, message = "地址不能超过255个字符")
        String address,

        @Size(max = 500, message = "备注不能超过500个字符")
        String remark
) {
}
