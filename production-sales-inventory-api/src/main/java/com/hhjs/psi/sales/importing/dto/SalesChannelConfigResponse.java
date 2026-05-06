package com.hhjs.psi.sales.importing.dto;

import com.hhjs.psi.sales.importing.entity.SalesChannelConfig;

import java.time.Instant;

public record SalesChannelConfigResponse(
        Long id,
        String code,
        String name,
        String sourceType,
        Boolean enabled,
        Integer sortOrder,
        String configJson,
        String remark,
        Instant createdAt,
        Instant updatedAt
) {
    public static SalesChannelConfigResponse from(SalesChannelConfig channel) {
        return new SalesChannelConfigResponse(
                channel.getId(),
                channel.getCode(),
                channel.getName(),
                channel.getSourceType().name(),
                channel.getEnabled(),
                channel.getSortOrder(),
                channel.getConfigJson(),
                channel.getRemark(),
                channel.getCreatedAt(),
                channel.getUpdatedAt()
        );
    }
}
