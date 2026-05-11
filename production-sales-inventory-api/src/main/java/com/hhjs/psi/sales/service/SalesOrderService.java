package com.hhjs.psi.sales.service;

import com.hhjs.psi.auth.repository.SysUserRepository;
import com.hhjs.psi.auth.security.SecurityUtils;
import com.hhjs.psi.common.exception.BusinessException;
import com.hhjs.psi.inventory.dto.StockOperationRequest;
import com.hhjs.psi.inventory.entity.Stock;
import com.hhjs.psi.inventory.entity.StockBatch;
import com.hhjs.psi.inventory.entity.StockRecordSubType;
import com.hhjs.psi.inventory.repository.StockBatchRepository;
import com.hhjs.psi.inventory.repository.StockRepository;
import com.hhjs.psi.inventory.service.InventoryService;
import com.hhjs.psi.sales.goods.entity.SalesGoods;
import com.hhjs.psi.sales.goods.entity.SalesGoodsComponent;
import com.hhjs.psi.sales.goods.repository.SalesGoodsRepository;
import com.hhjs.psi.sales.dto.SalesOrderItemRequest;
import com.hhjs.psi.sales.dto.SalesOrderItemResponse;
import com.hhjs.psi.sales.dto.SalesOrderRequest;
import com.hhjs.psi.sales.dto.SalesOrderResponse;
import com.hhjs.psi.sales.entity.SalesChannel;
import com.hhjs.psi.sales.entity.SalesOrder;
import com.hhjs.psi.sales.entity.SalesOrderItem;
import com.hhjs.psi.sales.entity.SalesOrderStatus;
import com.hhjs.psi.sales.importing.entity.ImportSourceType;
import com.hhjs.psi.sales.importing.entity.OrderImportBatch;
import com.hhjs.psi.sales.importing.entity.SalesChannelConfig;
import com.hhjs.psi.sales.importing.repository.SalesChannelConfigRepository;
import com.hhjs.psi.sales.repository.SalesOrderRepository;
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
public class SalesOrderService {

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final SalesOrderRepository salesOrderRepository;
    private final SalesGoodsRepository salesGoodsRepository;
    private final StockRepository stockRepository;
    private final StockBatchRepository stockBatchRepository;
    private final SysUserRepository sysUserRepository;
    private final InventoryService inventoryService;
    private final SalesChannelConfigRepository salesChannelConfigRepository;

    public SalesOrderService(
            SalesOrderRepository salesOrderRepository,
            SalesGoodsRepository salesGoodsRepository,
            StockRepository stockRepository,
            StockBatchRepository stockBatchRepository,
            SysUserRepository sysUserRepository,
            InventoryService inventoryService,
            SalesChannelConfigRepository salesChannelConfigRepository
    ) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesGoodsRepository = salesGoodsRepository;
        this.stockRepository = stockRepository;
        this.stockBatchRepository = stockBatchRepository;
        this.sysUserRepository = sysUserRepository;
        this.inventoryService = inventoryService;
        this.salesChannelConfigRepository = salesChannelConfigRepository;
    }

    @Transactional(readOnly = true)
    public Page<SalesOrderResponse> getOrders(int page, int size, String query, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return salesOrderRepository.searchOrders(normalizeOptional(query), parseStatus(status), pageable)
                .map(this::toResponse);
    }

    @Transactional
    public SalesOrderResponse createOrder(SalesOrderRequest request) {
        var currentSysUser = SecurityUtils.requireCurrentSysUser();
        var operator = sysUserRepository.findById(currentSysUser.id())
                .orElseThrow(() -> BusinessException.unauthorized("当前用户不存在"));
        SalesChannelConfig channelConfig = resolveChannelConfig(request.channelId(), request.channel());
        SalesOrder order = SalesOrder.create(
                generateOrderNo(),
                request.channel(),
                channelConfig,
                null,
                null,
                ImportSourceType.MANUAL,
                null,
                normalizeOptional(request.customerName()),
                normalizeOptional(request.customerPhone()),
                normalizeOptional(request.customerAddress()),
                operator,
                currentSysUser.username(),
                normalizeOptional(request.remark())
        );
        List<SalesOrderItem> items = buildItems(request.items());
        lockOrderItems(items);
        order.replaceItems(items);
        return toResponse(salesOrderRepository.save(order));
    }

    @Transactional
    public SalesOrderResponse updateOrder(Long orderId, SalesOrderRequest request) {
        SalesOrder order = findOrder(orderId);
        ensurePending(order, "只有待发货销售单可以修改");
        unlockOrderItems(order.getItems());
        List<SalesOrderItem> items = buildItems(request.items());
        lockOrderItems(items);
        order.updateBasicInfo(
                request.channel(),
                resolveChannelConfig(request.channelId(), request.channel()),
                normalizeOptional(request.customerName()),
                normalizeOptional(request.customerPhone()),
                normalizeOptional(request.customerAddress()),
                normalizeOptional(request.remark())
        );
        order.replaceItems(items);
        return toResponse(salesOrderRepository.save(order));
    }

    @Transactional
    public SalesOrder createImportedOrder(
            SalesChannel channel,
            SalesChannelConfig channelConfig,
            String externalOrderNo,
            OrderImportBatch importBatch,
            ImportSourceType sourceType,
            String customerName,
            String customerPhone,
            String customerAddress,
            String remark,
            List<SalesOrderItem> items
    ) {
        var currentSysUser = SecurityUtils.requireCurrentSysUser();
        var operator = sysUserRepository.findById(currentSysUser.id())
                .orElseThrow(() -> BusinessException.unauthorized("当前用户不存在"));
        SalesOrder order = SalesOrder.create(
                generateOrderNo(),
                channel,
                channelConfig,
                externalOrderNo,
                importBatch,
                sourceType,
                "导入批次: " + importBatch.getBatchNo(),
                normalizeOptional(customerName),
                normalizeOptional(customerPhone),
                normalizeOptional(customerAddress),
                operator,
                currentSysUser.username(),
                normalizeOptional(remark)
        );
        order.replaceItems(items);
        return salesOrderRepository.save(order);
    }

    @Transactional
    public SalesOrderResponse shipOrder(Long orderId) {
        SalesOrder order = findOrder(orderId);
        ensurePending(order, "只有待发货销售单可以发货出库");
        ensureOrderItemsLocked(order);
        order.getItems().forEach(item -> shipItem(order, item));
        order.markShipped(Instant.now());
        return toResponse(salesOrderRepository.save(order));
    }

    @Transactional
    public SalesOrderResponse completeOrder(Long orderId) {
        SalesOrder order = findOrder(orderId);
        if (order.getStatus() == SalesOrderStatus.COMPLETED) {
            throw BusinessException.badRequest("销售单已完成");
        }
        if (order.getStatus() != SalesOrderStatus.SHIPPED) {
            throw BusinessException.badRequest("只有已发货销售单可以完成");
        }
        order.markCompleted(Instant.now());
        return toResponse(salesOrderRepository.save(order));
    }

    @Transactional
    public SalesOrderResponse cancelOrder(Long orderId) {
        SalesOrder order = findOrder(orderId);
        if (order.getStatus() == SalesOrderStatus.CANCELLED) {
            throw BusinessException.badRequest("销售单已取消");
        }
        if (order.getStatus() == SalesOrderStatus.COMPLETED) {
            throw BusinessException.badRequest("已完成销售单不能取消");
        }
        if (order.getStatus() == SalesOrderStatus.PENDING) {
            unlockOrderItems(order.getItems());
        }
        order.cancel();
        return toResponse(salesOrderRepository.save(order));
    }

    private SalesOrder findOrder(Long orderId) {
        return salesOrderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> BusinessException.badRequest("销售单不存在: " + orderId));
    }

    private List<SalesOrderItem> buildItems(List<SalesOrderItemRequest> requests) {
        Set<Long> salesGoodsIds = new HashSet<>();
        List<SalesOrderItem> items = new ArrayList<>();
        for (SalesOrderItemRequest request : requests) {
            if (!salesGoodsIds.add(request.salesGoodsId())) {
                throw BusinessException.badRequest("同一销售单不能重复添加同一个商品");
            }
            SalesGoods salesGoods = salesGoodsRepository.findWithDetailsById(request.salesGoodsId())
                    .orElseThrow(() -> BusinessException.badRequest("销售商品不存在: " + request.salesGoodsId()));
            if (!Boolean.TRUE.equals(salesGoods.getEnabled())) {
                throw BusinessException.badRequest("销售商品已停用: " + salesGoods.getName());
            }
            if (salesGoods.getComponents().isEmpty()) {
                throw BusinessException.badRequest("销售商品未配置库存组成，不能销售: " + salesGoods.getName());
            }
            BigDecimal unitPrice = request.unitPrice() == null ? BigDecimal.ZERO : request.unitPrice();
            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw BusinessException.badRequest("销售单价不能小于0");
            }
            items.add(SalesOrderItem.create(salesGoods, request.quantity(), unitPrice));
        }
        return items;
    }

    private void lockOrderItems(List<SalesOrderItem> items) {
        for (SalesOrderItem item : items) {
            for (SalesGoodsComponent component : item.getSalesGoods().getComponents()) {
                int requiredQuantity = requiredInventoryQuantity(item, component);
                Stock stock = stockRepository.findByProductIdForUpdate(component.getProduct().getId())
                        .orElseThrow(() -> BusinessException.badRequest("库存产品不存在: " + component.getProduct().getName()));
                if (stock.getAvailableQuantity() < requiredQuantity) {
                    throw BusinessException.badRequest("库存产品可用库存不足: " + component.getProduct().getName() + "，需 " + requiredQuantity + "，可用 " + stock.getAvailableQuantity());
                }
                stock.lockQuantity(requiredQuantity);
                stockRepository.save(stock);
            }
        }
    }


    private void unlockOrderItems(List<SalesOrderItem> items) {
        for (SalesOrderItem item : items) {
            for (SalesGoodsComponent component : item.getSalesGoods().getComponents()) {
                int requiredQuantity = requiredInventoryQuantity(item, component);
                Stock stock = stockRepository.findByProductIdForUpdate(component.getProduct().getId())
                        .orElseThrow(() -> BusinessException.badRequest("库存产品不存在: " + component.getProduct().getName()));
                stock.unlockQuantity(requiredQuantity);
                stockRepository.save(stock);
            }
        }
    }

    private void shipItem(SalesOrder order, SalesOrderItem item) {
        for (SalesGoodsComponent component : item.getSalesGoods().getComponents()) {
            int remaining = requiredInventoryQuantity(item, component);
            List<StockBatch> batches = stockBatchRepository.findAvailableByProductId(component.getProduct().getId());
            if (batches.stream().mapToInt(StockBatch::getAvailableQuantity).sum() < remaining) {
                throw BusinessException.badRequest("库存产品批次可用库存不足: " + component.getProduct().getName());
            }
            for (StockBatch batch : batches) {
                if (remaining <= 0) {
                    break;
                }
                int outboundQuantity = Math.min(remaining, batch.getAvailableQuantity());
                inventoryService.outboundFromLocked(new StockOperationRequest(
                        component.getProduct().getId(),
                        null,
                        StockRecordSubType.SALES,
                        outboundQuantity,
                        order.getId(),
                        null,
                        batch.getId(),
                        order.getOrderNo(),
                        null,
                        null,
                        "销售出库: %s / %s / %s".formatted(order.getOrderNo(), item.getGoodsName(), normalizeCustomerName(order))
                ));
                remaining -= outboundQuantity;
            }
        }
    }

    private void ensureOrderItemsLocked(SalesOrder order) {
        for (SalesOrderItem item : order.getItems()) {
            for (SalesGoodsComponent component : item.getSalesGoods().getComponents()) {
                int requiredQuantity = requiredInventoryQuantity(item, component);
                Stock stock = stockRepository.findByProductIdForUpdate(component.getProduct().getId())
                        .orElseThrow(() -> BusinessException.badRequest("库存产品不存在: " + component.getProduct().getName()));
                if (stock.getLockedQuantity() >= requiredQuantity) {
                    continue;
                }
                int needLock = requiredQuantity - stock.getLockedQuantity();
                if (stock.getAvailableQuantity() < needLock) {
                    throw BusinessException.badRequest("库存产品未锁定且可用库存不足，无法发货: " + component.getProduct().getName() + "，需补锁 " + needLock + "，当前可用 " + stock.getAvailableQuantity());
                }
                stock.lockQuantity(needLock);
                stockRepository.save(stock);
            }
        }
    }

    private int requiredInventoryQuantity(SalesOrderItem item, SalesGoodsComponent component) {
        BigDecimal base = component.getQuantityPerUnit().multiply(BigDecimal.valueOf(item.getQuantity()));
        BigDecimal withLoss = base.multiply(BigDecimal.ONE.add(component.getLossRate()));
        return withLoss.setScale(0, java.math.RoundingMode.CEILING).intValue();
    }

    private void ensurePending(SalesOrder order, String message) {
        if (order.getStatus() != SalesOrderStatus.PENDING) {
            throw BusinessException.badRequest(message);
        }
    }

    private SalesOrderStatus parseStatus(String status) {
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
            return null;
        }
        try {
            return SalesOrderStatus.valueOf(status.trim());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest("不支持的销售单状态: " + status);
        }
    }

    private SalesOrderResponse toResponse(SalesOrder order) {
        return new SalesOrderResponse(
                order.getId(),
                order.getOrderNo(),
                order.getChannel(),
                order.getChannelConfig() == null ? null : order.getChannelConfig().getId(),
                toChannelText(order),
                order.getExternalOrderNo(),
                order.getImportBatch() == null ? null : order.getImportBatch().getId(),
                order.getSourceType() == null ? null : order.getSourceType().name(),
                order.getSourceRemark(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getCustomerAddress(),
                order.getTotalAmount(),
                order.getStatus(),
                toStatusText(order.getStatus()),
                order.getOrderDate(),
                order.getShipDate(),
                order.getCompleteDate(),
                order.getOperatorName(),
                order.getRemark(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems().stream().map(this::toItemResponse).toList()
        );
    }

    private SalesOrderItemResponse toItemResponse(SalesOrderItem item) {
        return new SalesOrderItemResponse(
                item.getId(),
                item.getSalesGoods().getId(),
                item.getGoodsCode(),
                item.getGoodsName(),
                item.getGoodsSpecification(),
                item.getGoodsUnit(),
                item.getGoodsCategory(),
                item.getExternalProductName(),
                item.getExternalSpecName(),
                item.getExternalQuantity(),
                item.getMapping() == null ? null : item.getMapping().getId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }

    private String toStatusText(SalesOrderStatus status) {
        return switch (status) {
            case PENDING -> "待发货";
            case SHIPPED -> "已发货";
            case COMPLETED -> "已完成";
            case CANCELLED -> "已取消";
        };
    }

    private String toChannelText(SalesOrder order) {
        if (order.getChannelConfig() != null && order.getChannelConfig().getName() != null) {
            return order.getChannelConfig().getName();
        }
        return switch (order.getChannel()) {
            case DOUYIN -> "抖音";
            case PINDUODUO -> "拼多多";
            case OFFLINE, WECHAT_GROUP, CONTRACT -> "线下";
        };
    }

    private SalesChannelConfig resolveChannelConfig(Long channelId, SalesChannel channel) {
        if (channelId != null) {
            return salesChannelConfigRepository.findById(channelId)
                    .orElseThrow(() -> BusinessException.badRequest("销售渠道不存在: " + channelId));
        }
        return salesChannelConfigRepository.findByCode(channel.name()).orElse(null);
    }

    private String normalizeCustomerName(SalesOrder order) {
        return order.getCustomerName() == null || order.getCustomerName().isBlank() ? "散客" : order.getCustomerName();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String generateOrderNo() {
        return "SO%s%d".formatted(
                LocalDateTime.now().format(ORDER_NO_FORMATTER),
                ThreadLocalRandom.current().nextInt(1000, 10000)
        );
    }
}
