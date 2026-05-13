package com.hhjs.psi.sales.goods.dto;

import com.hhjs.psi.sales.goods.entity.SalesSku;
import com.hhjs.psi.sales.goods.entity.SalesSkuComponent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public record SalesSkuResponse(
        Long id,
        String code,
        String name,
        String specName,
        String unit,
        BigDecimal perSkuPrice,
        Boolean enabled,
        Long salesGoodsId,
        String salesGoodsName,
        String remark,
        Instant createdAt,
        Instant updatedAt,
        List<ComponentResponse> components
) {
    public static SalesSkuResponse from(SalesSku sku) {
        return new SalesSkuResponse(
                sku.getId(),
                sku.getCode(),
                sku.getName(),
                sku.getSpecName(),
                sku.getUnit(),
                sku.getPerSkuPrice(),
                sku.getEnabled(),
                sku.getSalesGoods() != null ? sku.getSalesGoods().getId() : null,
                sku.getSalesGoods() != null ? sku.getSalesGoods().getName() : null,
                sku.getRemark(),
                sku.getCreatedAt(),
                sku.getUpdatedAt(),
                sku.getComponents().stream()
                        .sorted(Comparator.comparing(item -> item.getId() == null ? 0L : item.getId()))
                        .map(ComponentResponse::from)
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
            BigDecimal quantity,
            String remark
    ) {
        public static ComponentResponse from(SalesSkuComponent component) {
            return new ComponentResponse(
                    component.getId(),
                    component.getProduct().getId(),
                    component.getProduct().getCode(),
                    component.getProduct().getName(),
                    component.getProduct().getSpecification(),
                    component.getProduct().getUnit(),
                    component.getQuantity(),
                    component.getRemark()
            );
        }
    }
}
