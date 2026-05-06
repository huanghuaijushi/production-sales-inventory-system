package com.hhjs.psi.sales.dto;

import com.hhjs.psi.sales.entity.SalesChannel;
import com.hhjs.psi.sales.entity.SalesOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SalesOrderResponse(
        Long id,
        String orderNo,
        SalesChannel channel,
        Long channelId,
        String channelText,
        String externalOrderNo,
        Long importBatchId,
        String sourceType,
        String sourceRemark,
        String customerName,
        String customerPhone,
        String customerAddress,
        BigDecimal totalAmount,
        SalesOrderStatus status,
        String statusText,
        Instant orderDate,
        Instant shipDate,
        Instant completeDate,
        String operatorName,
        String remark,
        Instant createdAt,
        Instant updatedAt,
        List<SalesOrderItemResponse> items
) {
}
