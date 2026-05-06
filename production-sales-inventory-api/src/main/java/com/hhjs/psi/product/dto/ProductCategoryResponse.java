package com.hhjs.psi.product.dto;

import com.hhjs.psi.inventory.entity.ProductType;
import com.hhjs.psi.product.entity.ProductCategory;

import java.time.Instant;

public record ProductCategoryResponse(
        Long id,
        String name,
        ProductType type,
        String typeText,
        Integer sortOrder,
        Boolean enabled,
        String remark,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductCategoryResponse from(ProductCategory category) {
        ProductType type = category.getType();
        return new ProductCategoryResponse(
                category.getId(),
                category.getName(),
                type,
                switch (type) {
                    case FINISHED_PRODUCT -> "成品 / 半成品";
                    case RAW_MATERIAL -> "原料 / 包装";
                    case null -> "通用";
                },
                category.getSortOrder(),
                category.getEnabled(),
                category.getRemark(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
