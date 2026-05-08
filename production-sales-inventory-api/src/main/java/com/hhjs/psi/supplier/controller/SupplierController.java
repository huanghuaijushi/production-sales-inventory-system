package com.hhjs.psi.supplier.controller;

import com.hhjs.psi.common.dto.ApiResponse;
import com.hhjs.psi.common.dto.PageResponse;
import com.hhjs.psi.supplier.dto.SupplierRequest;
import com.hhjs.psi.supplier.dto.SupplierResponse;
import com.hhjs.psi.supplier.service.SupplierService;
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
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('purchase:view')")
    @GetMapping
    public ApiResponse<PageResponse<SupplierResponse>> getSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query
    ) {
        return ApiResponse.ok(PageResponse.from(supplierService.getSuppliers(page, size, query)));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('purchase:create')")
    @PostMapping
    public ApiResponse<SupplierResponse> createSupplier(@Valid @RequestBody SupplierRequest request) {
        return ApiResponse.ok(supplierService.createSupplier(request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('purchase:update')")
    @PutMapping("/{supplierId}")
    public ApiResponse<SupplierResponse> updateSupplier(
            @PathVariable Long supplierId,
            @Valid @RequestBody SupplierRequest request
    ) {
        return ApiResponse.ok(supplierService.updateSupplier(supplierId, request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('purchase:cancel')")
    @DeleteMapping("/{supplierId}")
    public ApiResponse<Void> deleteSupplier(@PathVariable Long supplierId) {
        supplierService.deleteSupplier(supplierId);
        return ApiResponse.ok();
    }
}
