package com.hhjs.psi.sales.goods.dto;

import com.hhjs.psi.sales.goods.entity.SalesGoods;
import com.hhjs.psi.sales.goods.entity.SalesGoodsChannelPrice;
import com.hhjs.psi.sales.goods.entity.SalesGoodsComponent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public record SalesGoodsResponse(
        Long id,
        String code,
        String name,
        String category,
        String specification,
        String unit,
        BigDecimal defaultPrice,
        Boolean enabled,
        String remark,
        Instant createdAt,
        Instant updatedAt,
        List<ComponentResponse> components,
        List<ChannelPriceResponse> channelPrices
) {
    public static SalesGoodsResponse from(SalesGoods goods) {
        return new SalesGoodsResponse(
                goods.getId(),
                goods.getCode(),
                goods.getName(),
                goods.getCategory(),
                goods.getSpecification(),
                goods.getUnit(),
                goods.getDefaultPrice(),
                goods.getEnabled(),
                goods.getRemark(),
                goods.getCreatedAt(),
                goods.getUpdatedAt(),
                goods.getComponents().stream()
                        .sorted(Comparator.comparing(item -> item.getId() == null ? 0L : item.getId()))
                        .map(ComponentResponse::from)
                        .toList(),
                goods.getChannelPrices().stream()
                        .sorted(Comparator.comparing(item -> item.getId() == null ? 0L : item.getId()))
                        .map(ChannelPriceResponse::from)
                        .toList()
        );
    }

    public record ComponentResponse(
            Long id,
            Long productId,
            String productCode,
            String productName,
            String productSpecification,
            String productUnit,
            BigDecimal quantityPerUnit,
            BigDecimal lossRate,
            String remark
    ) {
        public static ComponentResponse from(SalesGoodsComponent component) {
            return new ComponentResponse(
                    component.getId(),
                    component.getProduct().getId(),
                    component.getProduct().getCode(),
                    component.getProduct().getName(),
                    component.getProduct().getSpecification(),
                    component.getProduct().getUnit(),
                    component.getQuantityPerUnit(),
                    component.getLossRate(),
                    component.getRemark()
            );
        }
    }

    public record ChannelPriceResponse(
            Long id,
            Long channelId,
            String channelName,
            BigDecimal price,
            Boolean enabled,
            String remark
    ) {
        public static ChannelPriceResponse from(SalesGoodsChannelPrice price) {
            return new ChannelPriceResponse(
                    price.getId(),
                    price.getChannel().getId(),
                    price.getChannel().getName(),
                    price.getPrice(),
                    price.getEnabled(),
                    price.getRemark()
            );
        }
    }
}
