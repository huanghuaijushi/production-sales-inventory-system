package com.hhjs.psi.sales.importing.dto;

import com.hhjs.psi.sales.importing.entity.ExternalOrderRaw;

import java.time.Instant;
import java.util.List;

public record ExternalOrderRawResponse(
        Long id,
        Long batchId,
        Long channelId,
        String channelName,
        String externalOrderNo,
        String customerName,
        String customerPhone,
        String customerAddress,
        String buyerMessage,
        String sellerRemark,
        String status,
        String errorMessage,
        Long salesOrderId,
        Instant createdAt,
        List<ExternalOrderItemRawResponse> items
) {
    public static ExternalOrderRawResponse from(ExternalOrderRaw order) {
        return new ExternalOrderRawResponse(
                order.getId(),
                order.getBatch().getId(),
                order.getChannel().getId(),
                order.getChannel().getName(),
                order.getExternalOrderNo(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getCustomerAddress(),
                order.getBuyerMessage(),
                order.getSellerRemark(),
                order.getStatus().name(),
                order.getErrorMessage(),
                order.getSalesOrder() == null ? null : order.getSalesOrder().getId(),
                order.getCreatedAt(),
                order.getItems().stream().map(ExternalOrderItemRawResponse::from).toList()
        );
    }
}
