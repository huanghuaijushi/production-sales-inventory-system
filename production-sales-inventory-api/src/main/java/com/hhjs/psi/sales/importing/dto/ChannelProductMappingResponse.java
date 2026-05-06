package com.hhjs.psi.sales.importing.dto;

import com.hhjs.psi.sales.importing.entity.ChannelProductMapping;

import java.math.BigDecimal;
import java.time.Instant;

public record ChannelProductMappingResponse(
        Long id,
        Long channelId,
        String channelName,
        String externalProductName,
        String externalSpecName,
        String externalSkuCode,
        Long productId,
        String productCode,
        String productName,
        String productSpecification,
        String productUnit,
        BigDecimal quantityMultiplier,
        BigDecimal defaultUnitPrice,
        String matchType,
        Boolean enabled,
        Integer priority,
        String remark,
        Instant createdAt,
        Instant updatedAt
) {
    public static ChannelProductMappingResponse from(ChannelProductMapping mapping) {
        return new ChannelProductMappingResponse(
                mapping.getId(),
                mapping.getChannel().getId(),
                mapping.getChannel().getName(),
                mapping.getExternalProductName(),
                mapping.getExternalSpecName(),
                mapping.getExternalSkuCode(),
                mapping.getProduct().getId(),
                mapping.getProduct().getCode(),
                mapping.getProduct().getName(),
                mapping.getProduct().getSpecification(),
                mapping.getProduct().getUnit(),
                mapping.getQuantityMultiplier(),
                mapping.getDefaultUnitPrice(),
                mapping.getMatchType().name(),
                mapping.getEnabled(),
                mapping.getPriority(),
                mapping.getRemark(),
                mapping.getCreatedAt(),
                mapping.getUpdatedAt()
        );
    }
}
