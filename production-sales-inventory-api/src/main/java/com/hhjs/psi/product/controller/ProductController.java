package com.hhjs.psi.product.controller;

import com.hhjs.psi.common.dto.ApiResponse;
import com.hhjs.psi.common.dto.PageResponse;
import com.hhjs.psi.product.dto.ProductRequest;
import com.hhjs.psi.product.dto.ProductResponse;
import com.hhjs.psi.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('product:view')")
    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(PageResponse.from(productService.getAllProducts(page, size)));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('product:create')")
    @PostMapping
    public ApiResponse<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ApiResponse.ok(product);
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('product:update')")
    @PutMapping("/{productId}")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest request
    ) {
        return ApiResponse.ok(productService.updateProduct(productId, request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('product:delete')")
    @DeleteMapping("/{productId}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ApiResponse.ok();
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('product:view')")
    @GetMapping("/search")
    public ApiResponse<PageResponse<ProductResponse>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(PageResponse.from(productService.searchProducts(query, page, size)));
    }
}
