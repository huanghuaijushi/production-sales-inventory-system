package com.hhjs.psi.sales.dto;

import com.hhjs.psi.sales.entity.SalesChannel;
import com.hhjs.psi.sales.entity.SalesOrder;
import com.hhjs.psi.sales.entity.SalesOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
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
    public static SalesOrderResponse from(SalesOrder order) {
        return new SalesOrderResponse(
                order.getId(),
                order.getOrderNo(),
                order.getChannel(),
                order.getChannelConfig() != null ? order.getChannelConfig().getId() : null,
                channelText(order.getChannel()),
                order.getExternalOrderNo(),
                order.getImportBatch() != null ? order.getImportBatch().getId() : null,
                order.getSourceType() != null ? order.getSourceType().name() : null,
                order.getSourceRemark(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getCustomerAddress(),
                order.getTotalAmount(),
                order.getStatus(),
                statusText(order.getStatus()),
                order.getOrderDate(),
                order.getShipDate(),
                order.getCompleteDate(),
                order.getOperatorName(),
                order.getRemark(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems().stream()
                        .sorted(Comparator.comparing(item -> item.getId() == null ? 0L : item.getId()))
                        .map(SalesOrderItemResponse::from)
                        .toList()
        );
    }

    private static String channelText(SalesChannel channel) {
        return switch (channel) {
            case DOUYIN -> "抖音";
            case PINDUODUO -> "拼多多";
            case OFFLINE -> "线下";
            case WECHAT_GROUP -> "微信群";
            case CONTRACT -> "合同";
        };
    }

    private static String statusText(SalesOrderStatus status) {
        return switch (status) {
            case PENDING -> "待处理";
            case SHIPPED -> "已发货";
            case COMPLETED -> "已完成";
            case CANCELLED -> "已取消";
        };
    }
}
