package com.hhjs.psi.product.service;

import com.hhjs.psi.common.exception.BusinessException;
import com.hhjs.psi.inventory.entity.ProductType;
import com.hhjs.psi.product.dto.ProductCategoryRequest;
import com.hhjs.psi.product.dto.ProductCategoryResponse;
import com.hhjs.psi.product.entity.ProductCategory;
import com.hhjs.psi.product.repository.ProductCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    public ProductCategoryService(ProductCategoryRepository productCategoryRepository) {
        this.productCategoryRepository = productCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> getCategories(String type, boolean enabledOnly) {
        ProductType productType = parseNullableProductType(type);
        List<ProductCategory> categories = enabledOnly
                ? productCategoryRepository.findEnabledByOptionalType(productType)
                : productCategoryRepository.findByOptionalType(productType);
        return categories.stream().map(ProductCategoryResponse::from).toList();
    }

    @Transactional
    public ProductCategoryResponse createCategory(ProductCategoryRequest request) {
        String name = normalizeName(request.name());
        ProductType type = parseNullableProductType(request.type());
        if (productCategoryRepository.existsByNameAndType(name, type)) {
            throw BusinessException.conflict("分类已存在: " + name);
        }
        ProductCategory category = ProductCategory.create(
                name,
                type,
                request.sortOrder(),
                normalizeOptional(request.remark())
        );
        category.update(name, type, request.sortOrder(), request.enabled(), normalizeOptional(request.remark()));
        return ProductCategoryResponse.from(productCategoryRepository.save(category));
    }

    @Transactional
    public ProductCategoryResponse updateCategory(Long categoryId, ProductCategoryRequest request) {
        ProductCategory category = productCategoryRepository.findById(categoryId)
                .orElseThrow(() -> BusinessException.badRequest("分类不存在: " + categoryId));
        String name = normalizeName(request.name());
        ProductType type = parseNullableProductType(request.type());
        if (productCategoryRepository.existsByNameAndTypeAndIdNot(name, type, categoryId)) {
            throw BusinessException.conflict("分类已存在: " + name);
        }
        category.update(name, type, request.sortOrder(), request.enabled(), normalizeOptional(request.remark()));
        return ProductCategoryResponse.from(productCategoryRepository.save(category));
    }

    private ProductType parseNullableProductType(String type) {
        if (type == null || type.isBlank() || "ALL".equalsIgnoreCase(type)) {
            return null;
        }
        return switch (type.trim().toUpperCase(Locale.ROOT)) {
            case "FINISHED_PRODUCT", "成品" -> ProductType.FINISHED_PRODUCT;
            case "RAW_MATERIAL", "原料" -> ProductType.RAW_MATERIAL;
            case "PACKAGING_MATERIAL", "包装" -> ProductType.PACKAGING_MATERIAL;
            case "SEMI_FINISHED_PRODUCT", "半成品" -> ProductType.SEMI_FINISHED_PRODUCT;
            default -> throw BusinessException.badRequest("不支持的产品类型: " + type);
        };
    }

    private String normalizeName(String value) {
        if (value == null || value.isBlank()) {
            throw BusinessException.badRequest("分类名称不能为空");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
