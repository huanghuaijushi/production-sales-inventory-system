package com.hhjs.psi.purchase.service;

import com.hhjs.psi.auth.security.SecurityUtils;
import com.hhjs.psi.common.exception.BusinessException;
import com.hhjs.psi.inventory.dto.StockOperationRequest;
import com.hhjs.psi.inventory.dto.StockRecordResponse;
import com.hhjs.psi.inventory.entity.Product;
import com.hhjs.psi.inventory.entity.ProductType;
import com.hhjs.psi.inventory.entity.StockRecordSubType;
import com.hhjs.psi.inventory.entity.StockRecordType;
import com.hhjs.psi.inventory.repository.ProductRepository;
import com.hhjs.psi.inventory.service.InventoryService;
import com.hhjs.psi.purchase.dto.PurchaseOrderItemRequest;
import com.hhjs.psi.purchase.dto.PurchaseOrderItemResponse;
import com.hhjs.psi.purchase.dto.PurchaseOrderRequest;
import com.hhjs.psi.purchase.dto.PurchaseOrderResponse;
import com.hhjs.psi.purchase.entity.PurchaseOrder;
import com.hhjs.psi.purchase.entity.PurchaseOrderItem;
import com.hhjs.psi.purchase.entity.PurchaseOrderStatus;
import com.hhjs.psi.purchase.repository.PurchaseOrderRepository;
import com.hhjs.psi.supplier.entity.Supplier;
import com.hhjs.psi.supplier.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PurchaseOrderService {

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    public PurchaseOrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            InventoryService inventoryService
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrderResponse> getOrders(int page, int size, String query, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return purchaseOrderRepository.searchOrders(
                        normalizeOptional(query),
                        parseStatus(status),
                        pageable
                )
                .map(this::toResponse);
    }

    @Transactional
    public PurchaseOrderResponse createOrder(PurchaseOrderRequest request) {
        Supplier supplier = getEnabledSupplier(request.supplierId());
        var currentSysUser = SecurityUtils.requireCurrentSysUser();
        PurchaseOrder order = PurchaseOrder.create(
                generateOrderNo(),
                supplier,
                resolveStatus(request.draft()),
                request.expectedArrivalDate(),
                currentSysUser.id(),
                currentSysUser.username(),
                normalizeOptional(request.remark())
        );
        order.replaceItems(buildItems(request.items()));
        return toResponse(purchaseOrderRepository.save(order));
    }

    @Transactional
    public PurchaseOrderResponse updateOrder(Long orderId, PurchaseOrderRequest request) {
        PurchaseOrder order = purchaseOrderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> BusinessException.badRequest("采购单不存在: " + orderId));
        ensureEditable(order);

        Supplier supplier = getEnabledSupplier(request.supplierId());
        order.updateBasicInfo(
                supplier,
                resolveStatus(request.draft()),
                request.expectedArrivalDate(),
                normalizeOptional(request.remark())
        );
        order.replaceItems(buildItems(request.items()));

        return toResponse(purchaseOrderRepository.save(order));
    }

    @Transactional
    public PurchaseOrderResponse inbound(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> BusinessException.badRequest("采购单不存在: " + orderId));
        if (order.getStatus() == PurchaseOrderStatus.INBOUNDED) {
            throw BusinessException.badRequest("采购单已入库");
        }
        if (order.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw BusinessException.badRequest("已取消的采购单不能入库");
        }
        if (order.getStatus() == PurchaseOrderStatus.DRAFT) {
            throw BusinessException.badRequest("草稿采购单不能入库，请先保存为待入库");
        }

        order.getItems().forEach(item -> {
            StockRecordResponse record = inventoryService.inbound(new StockOperationRequest(
                    item.getProduct().getId(),
                    StockRecordType.IN,
                    StockRecordSubType.PURCHASE,
                    item.getQuantity(),
                    order.getId(),
                    null,
                    null,
                    null,
                    "%s-%s".formatted(order.getOrderNo(), item.getProductCode()),
                    order.getExpectedArrivalDate(),
                    null,
                    "采购入库: %s / %s".formatted(order.getOrderNo(), order.getSupplier().getName())
            ));
            if (record.batchNo() == null) {
                throw BusinessException.badRequest("采购入库批次生成失败: " + item.getProductName());
            }
        });

        order.markInbound(Instant.now());
        return toResponse(purchaseOrderRepository.save(order));
    }

    @Transactional
    public PurchaseOrderResponse cancel(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> BusinessException.badRequest("采购单不存在: " + orderId));
        if (order.getStatus() == PurchaseOrderStatus.INBOUNDED) {
            throw BusinessException.badRequest("已入库的采购单不能取消");
        }
        order.cancel();
        return toResponse(purchaseOrderRepository.save(order));
    }

    private List<PurchaseOrderItem> buildItems(List<PurchaseOrderItemRequest> requests) {
        Set<Long> productIds = new HashSet<>();
        List<PurchaseOrderItem> items = new ArrayList<>();
        for (PurchaseOrderItemRequest request : requests) {
            if (!productIds.add(request.productId())) {
                throw BusinessException.badRequest("同一采购单不能重复添加同一个商品");
            }

            Product product = productRepository.findById(request.productId())
                    .orElseThrow(() -> BusinessException.badRequest("商品不存在: " + request.productId()));
            if (!Boolean.TRUE.equals(product.getEnabled())) {
                throw BusinessException.badRequest("商品已停用: " + product.getName());
            }
            if (product.getType() != ProductType.RAW_MATERIAL) {
                throw BusinessException.badRequest("采购单只能采购原材料，成品请通过生产入库: " + product.getName());
            }

            BigDecimal unitPrice = request.unitPrice() == null ? BigDecimal.ZERO : request.unitPrice();
            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw BusinessException.badRequest("采购单价不能小于0");
            }

            items.add(PurchaseOrderItem.create(product, request.quantity(), unitPrice));
        }
        return items;
    }

    private void ensureEditable(PurchaseOrder order) {
        if (order.getStatus() == PurchaseOrderStatus.INBOUNDED) {
            throw BusinessException.badRequest("已入库的采购单不能修改");
        }
        if (order.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw BusinessException.badRequest("已取消的采购单不能修改");
        }
    }

    private Supplier getEnabledSupplier(Long supplierId) {
        return supplierRepository.findByIdAndEnabledTrue(supplierId)
                .orElseThrow(() -> BusinessException.badRequest("供应商不存在或已停用: " + supplierId));
    }

    private PurchaseOrderStatus resolveStatus(Boolean draft) {
        return Boolean.TRUE.equals(draft) ? PurchaseOrderStatus.DRAFT : PurchaseOrderStatus.PENDING_INBOUND;
    }

    private PurchaseOrderStatus parseStatus(String status) {
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
            return null;
        }
        try {
            return PurchaseOrderStatus.valueOf(status.trim());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest("不支持的采购单状态: " + status);
        }
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder order) {
        List<PurchaseOrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();
        return new PurchaseOrderResponse(
                order.getId(),
                order.getOrderNo(),
                order.getSupplier().getId(),
                order.getSupplier().getName(),
                order.getStatus(),
                toStatusText(order.getStatus()),
                order.getExpectedArrivalDate(),
                order.getTotalAmount(),
                order.getOperatorName(),
                order.getRemark(),
                order.getInboundAt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemResponses
        );
    }

    private PurchaseOrderItemResponse toItemResponse(PurchaseOrderItem item) {
        return new PurchaseOrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProductCode(),
                item.getProductName(),
                item.getProductSpecification(),
                item.getProductUnit(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getAmount()
        );
    }

    private String toStatusText(PurchaseOrderStatus status) {
        return switch (status) {
            case DRAFT -> "草稿";
            case PENDING_INBOUND -> "待入库";
            case INBOUNDED -> "已入库";
            case CANCELLED -> "已取消";
        };
    }

    private String generateOrderNo() {
        return "PO%s%d".formatted(
                LocalDateTime.now().format(ORDER_NO_FORMATTER),
                ThreadLocalRandom.current().nextInt(1000, 10000)
        );
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
