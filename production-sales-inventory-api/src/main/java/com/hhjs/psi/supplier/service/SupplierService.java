package com.hhjs.psi.supplier.service;

import com.hhjs.psi.common.exception.BusinessException;
import com.hhjs.psi.supplier.dto.SupplierRequest;
import com.hhjs.psi.supplier.dto.SupplierResponse;
import com.hhjs.psi.supplier.entity.Supplier;
import com.hhjs.psi.supplier.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> getSuppliers(int page, int size, String query) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String normalizedQuery = normalizeOptional(query);
        Page<Supplier> suppliers = normalizedQuery == null
                ? supplierRepository.findByEnabledTrue(pageable)
                : supplierRepository.searchEnabledSuppliers(normalizedQuery, pageable);
        return suppliers.map(this::toResponse);
    }

    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        String name = request.name().trim();
        if (supplierRepository.existsByName(name)) {
            throw BusinessException.conflict("供应商名称已存在: " + name);
        }

        Supplier supplier = Supplier.create(
                name,
                normalizeOptional(request.contactName()),
                normalizeOptional(request.phone()),
                normalizeOptional(request.address()),
                normalizeOptional(request.remark())
        );
        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse updateSupplier(Long supplierId, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> BusinessException.badRequest("供应商不存在: " + supplierId));
        if (!Boolean.TRUE.equals(supplier.getEnabled())) {
            throw BusinessException.badRequest("供应商已停用");
        }

        String name = request.name().trim();
        if (supplierRepository.existsByNameAndIdNot(name, supplierId)) {
            throw BusinessException.conflict("供应商名称已存在: " + name);
        }

        supplier.update(
                name,
                normalizeOptional(request.contactName()),
                normalizeOptional(request.phone()),
                normalizeOptional(request.address()),
                normalizeOptional(request.remark())
        );
        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public void deleteSupplier(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> BusinessException.badRequest("供应商不存在: " + supplierId));
        if (!Boolean.TRUE.equals(supplier.getEnabled())) {
            return;
        }

        supplier.disable();
        supplierRepository.save(supplier);
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactName(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.getRemark(),
                supplier.getEnabled(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt()
        );
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
