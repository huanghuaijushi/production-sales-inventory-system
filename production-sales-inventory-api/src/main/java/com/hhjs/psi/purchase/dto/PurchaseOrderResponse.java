package com.hhjs.psi.purchase.dto;

import com.hhjs.psi.purchase.entity.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PurchaseOrderResponse(
        Long id,
        String orderNo,
        Long supplierId,
        String supplierName,
        PurchaseOrderStatus status,
        String statusText,
        LocalDate expectedArrivalDate,
        BigDecimal totalAmount,
        String operatorName,
        String remark,
        Instant inboundAt,
        Instant createdAt,
        Instant updatedAt,
        List<PurchaseOrderItemResponse> items
) {
}
