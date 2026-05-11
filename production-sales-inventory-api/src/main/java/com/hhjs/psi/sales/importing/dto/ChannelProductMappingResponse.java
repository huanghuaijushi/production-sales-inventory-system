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
        Long salesGoodsId,
        String salesGoodsCode,
        String salesGoodsName,
        String salesGoodsSpecification,
        String salesGoodsUnit,
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
                mapping.getSalesGoods().getId(),
                mapping.getSalesGoods().getCode(),
                mapping.getSalesGoods().getName(),
                mapping.getSalesGoods().getSpecification(),
                mapping.getSalesGoods().getUnit(),
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
