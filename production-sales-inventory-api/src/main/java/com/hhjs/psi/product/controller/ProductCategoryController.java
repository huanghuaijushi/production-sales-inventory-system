package com.hhjs.psi.product.controller;

import com.hhjs.psi.common.dto.ApiResponse;
import com.hhjs.psi.product.dto.ProductCategoryRequest;
import com.hhjs.psi.product.dto.ProductCategoryResponse;
import com.hhjs.psi.product.service.ProductCategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product-categories")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @GetMapping
    public ApiResponse<List<ProductCategoryResponse>> getCategories(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "true") boolean enabledOnly
    ) {
        return ApiResponse.ok(productCategoryService.getCategories(type, enabledOnly));
    }

    @PostMapping
    public ApiResponse<ProductCategoryResponse> createCategory(@Valid @RequestBody ProductCategoryRequest request) {
        return ApiResponse.ok(productCategoryService.createCategory(request));
    }

    @PutMapping("/{categoryId}")
    public ApiResponse<ProductCategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody ProductCategoryRequest request
    ) {
        return ApiResponse.ok(productCategoryService.updateCategory(categoryId, request));
    }
}
