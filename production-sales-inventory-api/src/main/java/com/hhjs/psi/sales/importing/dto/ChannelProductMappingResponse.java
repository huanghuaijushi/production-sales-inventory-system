package com.hhjs.psi.sales.importing.dto;

import com.hhjs.psi.sales.importing.entity.ChannelProductMapping;

import java.time.Instant;

public record ChannelProductMappingResponse(
        Long id,
        Long channelId,
        String channelName,
        String externalProductName,
        String externalSpecName,
        String externalSkuCode,
        Long salesSkuId,
        String salesSkuCode,
        String salesSkuName,
        String salesSkuSpecName,
        String salesSkuUnit,
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
                mapping.getSalesSku().getId(),
                mapping.getSalesSku().getCode(),
                mapping.getSalesSku().getName(),
                mapping.getSalesSku().getSpecName(),
                mapping.getSalesSku().getUnit(),
                mapping.getMatchType().name(),
                mapping.getEnabled(),
                mapping.getPriority(),
                mapping.getRemark(),
                mapping.getCreatedAt(),
                mapping.getUpdatedAt()
        );
    }
}
