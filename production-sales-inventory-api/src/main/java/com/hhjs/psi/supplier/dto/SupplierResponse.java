package com.hhjs.psi.supplier.dto;

import java.time.Instant;

public record SupplierResponse(
        Long id,
        String name,
        String contactName,
        String phone,
        String address,
        String remark,
        Boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
