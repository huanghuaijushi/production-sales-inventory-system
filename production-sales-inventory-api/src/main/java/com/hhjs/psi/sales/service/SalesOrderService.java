package com.hhjs.psi.sales.service;

import com.hhjs.psi.auth.entity.SysUser;
import com.hhjs.psi.auth.repository.SysUserRepository;
import com.hhjs.psi.auth.security.SecurityUtils;
import com.hhjs.psi.common.exception.BusinessException;
import com.hhjs.psi.inventory.entity.Product;
import com.hhjs.psi.inventory.entity.Stock;
import com.hhjs.psi.inventory.entity.StockBatch;
import com.hhjs.psi.inventory.entity.StockRecord;
import com.hhjs.psi.inventory.entity.StockRecordSubType;
import com.hhjs.psi.inventory.entity.StockRecordType;
import com.hhjs.psi.inventory.repository.StockBatchRepository;
import com.hhjs.psi.inventory.repository.StockRecordRepository;
import com.hhjs.psi.inventory.repository.StockRepository;
import com.hhjs.psi.sales.dto.SalesOrderItemRequest;
import com.hhjs.psi.sales.dto.SalesOrderItemResponse;
import com.hhjs.psi.sales.dto.SalesOrderRequest;
import com.hhjs.psi.sales.dto.SalesOrderResponse;
import com.hhjs.psi.sales.entity.SalesChannel;
import com.hhjs.psi.sales.entity.SalesOrder;
import com.hhjs.psi.sales.entity.SalesOrderItem;
import com.hhjs.psi.sales.entity.SalesOrderStatus;
import com.hhjs.psi.sales.entity.SalesOrderStockDeduction;
import com.hhjs.psi.sales.goods.entity.SalesSku;
import com.hhjs.psi.sales.goods.entity.SalesSkuComponent;
import com.hhjs.psi.sales.goods.repository.SalesSkuRepository;
import com.hhjs.psi.sales.importing.entity.ImportSourceType;
import com.hhjs.psi.sales.importing.entity.OrderImportBatch;
import com.hhjs.psi.sales.importing.entity.SalesChannelConfig;
import com.hhjs.psi.sales.importing.repository.SalesChannelConfigRepository;
import com.hhjs.psi.sales.repository.SalesOrderItemRepository;
import com.hhjs.psi.sales.repository.SalesOrderRepository;
import com.hhjs.psi.sales.repository.SalesOrderStockDeductionRepository;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SalesOrderService {

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final SalesOrderRepository orderRepository;
    private final SalesOrderItemRepository itemRepository;
    private final SalesSkuRepository salesSkuRepository;
    private final StockRepository stockRepository;
    private final StockBatchRepository batchRepository;
    private final StockRecordRepository stockRecordRepository;
    private final SalesOrderStockDeductionRepository deductionRepository;
    private final SysUserRepository sysUserRepository;
    private final SalesChannelConfigRepository channelConfigRepository;
    private final EntityManager em;

    public SalesOrderService(SalesOrderRepository orderRepository, SalesOrderItemRepository itemRepository,
                             SalesSkuRepository salesSkuRepository, StockRepository stockRepository,
                             StockBatchRepository batchRepository, StockRecordRepository stockRecordRepository,
                             SalesOrderStockDeductionRepository deductionRepository,
                             SysUserRepository sysUserRepository,
                             SalesChannelConfigRepository channelConfigRepository,
                             EntityManager em) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.salesSkuRepository = salesSkuRepository;
        this.stockRepository = stockRepository;
        this.batchRepository = batchRepository;
        this.stockRecordRepository = stockRecordRepository;
        this.deductionRepository = deductionRepository;
        this.sysUserRepository = sysUserRepository;
        this.channelConfigRepository = channelConfigRepository;
        this.em = em;
    }

    @Transactional(readOnly = true)
    public Page<SalesOrderResponse> getOrders(int page, int size, SalesOrderStatus status, String query) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate", "id"));
        Page<SalesOrder> orders = orderRepository.searchOrders(query != null ? query.trim() : null, status, pageable);
        return orders.map(SalesOrderResponse::from);
    }

    @Transactional(readOnly = true)
    public SalesOrderResponse getOrder(Long id) {
        return SalesOrderResponse.from(findOrder(id));
    }

    @Transactional
    public SalesOrderResponse create(SalesOrderRequest request) {
        var currentSysUser = SecurityUtils.requireCurrentSysUser();
        SysUser operator = sysUserRepository.findById(currentSysUser.id())
                .orElseThrow(() -> BusinessException.unauthorized("当前用户不存在"));
        String orderNo = "SO" + Instant.now().toString().replace("-", "").replace(":", "").replace("T", "").replace("Z", "").substring(0, 14) + String.format("%04d", ThreadLocalRandom.current().nextInt(1000, 10000));
        SalesOrder order = SalesOrder.create(
                orderNo,
                request.channel(),
                null,
                null,
                null,
                null,
                null,
                normalizeOptional(request.customerName()),
                normalizeOptional(request.customerPhone()),
                normalizeOptional(request.customerAddress()),
                operator,
                currentSysUser.username(),
                normalizeOptional(request.remark())
        );
        order = orderRepository.save(order);
        List<SalesOrderItem> items = new ArrayList<>();
        for (SalesOrderItemRequest itemRequest : request.items()) {
            SalesSku sku = salesSkuRepository.findById(itemRequest.salesSkuId())
                    .orElseThrow(() -> BusinessException.badRequest("销售SKU不存在: " + itemRequest.salesSkuId()));
            SalesOrderItem item = SalesOrderItem.create(sku, itemRequest.quantity(), itemRequest.unitPrice());
            item.attachTo(order);
            items.add(item);
        }
        itemRepository.saveAll(items);
        return SalesOrderResponse.from(findOrder(order.getId()));
    }

    @Transactional
    public SalesOrderResponse updateOrder(Long id, SalesOrderRequest request) {
        SalesOrder order = findOrder(id);
        SalesChannelConfig channelConfig = request.channelId() != null
                ? channelConfigRepository.findById(request.channelId()).orElse(null)
                : null;
        order.updateBasicInfo(request.channel(), channelConfig,
                request.customerName(), request.customerPhone(), request.customerAddress(), request.remark());
        updateOrderItems(order, request.items());
        em.flush();
        em.refresh(order);
        return SalesOrderResponse.from(order);
    }

    @Transactional
    public SalesOrderResponse updateOrderItems(Long orderId, List<SalesOrderItemRequest> itemRequests) {
        SalesOrder order = findOrder(orderId);
        updateOrderItems(order, itemRequests);
        em.flush();
        em.refresh(order);
        return SalesOrderResponse.from(order);
    }

    private void updateOrderItems(SalesOrder order, List<SalesOrderItemRequest> itemRequests) {
        if (order.getStatus() != SalesOrderStatus.PENDING) {
            throw BusinessException.badRequest("只能修改待处理状态的订单");
        }
        List<SalesOrderItem> newItems = new ArrayList<>();
        for (SalesOrderItemRequest itemRequest : itemRequests) {
            SalesSku sku = salesSkuRepository.findById(itemRequest.salesSkuId())
                    .orElseThrow(() -> BusinessException.badRequest("销售SKU不存在: " + itemRequest.salesSkuId()));
            SalesOrderItem item = SalesOrderItem.create(sku, itemRequest.quantity(), itemRequest.unitPrice());
            item.attachTo(order);
            newItems.add(item);
        }
        order.replaceItems(newItems);
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
        SysUser operator = sysUserRepository.findById(currentSysUser.id())
                .orElseThrow(() -> BusinessException.unauthorized("当前用户不存在"));
        String orderNo = "SO" + Instant.now().toString().replace("-", "").replace(":", "").replace("T", "").replace("Z", "").substring(0, 14) + String.format("%04d", ThreadLocalRandom.current().nextInt(1000, 10000));
        SalesOrder order = SalesOrder.create(
                orderNo,
                channel,
                channelConfig,
                externalOrderNo,
                importBatch,
                sourceType,
                null,
                customerName,
                customerPhone,
                customerAddress,
                operator,
                currentSysUser.username(),
                remark
        );
        order = orderRepository.save(order);
        order.replaceItems(items);
        orderRepository.save(order);
        return order;
    }

    @Transactional
    public SalesOrderResponse confirmOrder(Long id) {
        SalesOrder order = findOrder(id);
        if (order.getStatus() != SalesOrderStatus.PENDING) {
            throw BusinessException.badRequest("只能确认待处理状态的订单");
        }
        ensureOrderItemsLocked(order);
        return SalesOrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public SalesOrderResponse cancelOrder(Long id) {
        SalesOrder order = findOrder(id);
        if (order.getStatus() == SalesOrderStatus.CANCELLED) {
            throw BusinessException.badRequest("订单已取消");
        }
        if (order.getStatus() == SalesOrderStatus.PENDING) {
            unlockOrderItems(order);
        }
        order.cancel();
        return SalesOrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public SalesOrderResponse shipOrder(Long id) {
        SalesOrder order = findOrder(id);
        if (order.getStatus() != SalesOrderStatus.PENDING) {
            throw BusinessException.badRequest("只能对已确认的订单进行发货");
        }
        order.markShipped(Instant.now());
        return SalesOrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public SalesOrderResponse completeOrder(Long id) {
        SalesOrder order = findOrder(id);
        if (order.getStatus() != SalesOrderStatus.SHIPPED) {
            throw BusinessException.badRequest("只能对已发货的订单进行完成");
        }
        order.markCompleted(Instant.now());
        return SalesOrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public SalesOrderResponse shipItem(Long orderId, Long itemId, Long shippingBatchId, Integer shippingQuantity) {
        SalesOrder order = findOrder(orderId);
        if (order.getStatus() != SalesOrderStatus.PENDING) {
            throw BusinessException.badRequest("只能对已确认的订单进行发货");
        }
        SalesOrderItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> BusinessException.badRequest("订单明细不存在: " + itemId));
        if (!item.getOrder().getId().equals(orderId)) {
            throw BusinessException.badRequest("订单明细不属于该订单");
        }
        var currentSysUser = SecurityUtils.requireCurrentSysUser();
        SalesSku sku = item.getSalesSku();
        if (sku.getComponents().isEmpty()) {
            throw BusinessException.badRequest("SKU未配置库存组成，无法发货: " + sku.getCode());
        }
        StockBatch batch = batchRepository.findById(shippingBatchId)
                .orElseThrow(() -> BusinessException.badRequest("批次不存在: " + shippingBatchId));
        for (SalesSkuComponent skuComponent : sku.getComponents()) {
            Product product = skuComponent.getProduct();
            BigDecimal quantityPerSku = skuComponent.getQuantity();
            BigDecimal totalRequired = quantityPerSku.multiply(BigDecimal.valueOf(shippingQuantity));
            Stock stock = stockRepository.findByProductIdForUpdate(product.getId())
                    .orElseThrow(() -> BusinessException.badRequest("库存产品没有库存记录: " + product.getName()));
            int beforeQty = stock.getAvailableQuantity();
            stock.shipLockedQuantity(totalRequired.intValue());
            stockRepository.save(stock);
            StockRecord record = StockRecord.create(
                    generateRecordNo(),
                    product,
                    StockRecordType.OUT,
                    StockRecordSubType.SALES,
                    -totalRequired.intValue(),
                    beforeQty,
                    stock.getAvailableQuantity(),
                    currentSysUser.id(),
                    currentSysUser.username(),
                    "销售发货扣减: " + order.getOrderNo()
            );
            stockRecordRepository.save(record);
            SalesOrderStockDeduction deduction = SalesOrderStockDeduction.create(item, product, totalRequired.intValue(), batch);
            deductionRepository.save(deduction);
        }
        em.flush();
        em.refresh(order);
        return SalesOrderResponse.from(order);
    }

    private SalesOrder findOrder(Long id) {
        return orderRepository.findWithDetailsById(id)
                .orElseThrow(() -> BusinessException.badRequest("销售订单不存在: " + id));
    }

    private void ensureOrderItemsLocked(SalesOrder order) {
        var currentSysUser = SecurityUtils.requireCurrentSysUser();
        for (SalesOrderItem item : order.getItems()) {
            SalesSku sku = item.getSalesSku();
            if (sku.getComponents().isEmpty()) {
                throw BusinessException.badRequest("SKU未配置库存组成，无法锁定库存: " + sku.getCode());
            }
            for (SalesSkuComponent skuComponent : sku.getComponents()) {
                Product product = skuComponent.getProduct();
                BigDecimal quantityPerSku = skuComponent.getQuantity();
                BigDecimal totalRequired = quantityPerSku.multiply(BigDecimal.valueOf(item.getQuantity()));
                Stock stock = stockRepository.findByProductIdForUpdate(product.getId())
                        .orElseThrow(() -> BusinessException.badRequest("库存产品没有库存记录: " + product.getName()));
                if (stock.getAvailableQuantity() < totalRequired.intValue()) {
                    throw BusinessException.badRequest(
                            String.format("产品[%s]库存不足，需要%d，可用%d",
                                    product.getName(), totalRequired.intValue(), stock.getAvailableQuantity()));
                }
                int beforeQty = stock.getAvailableQuantity();
                stock.lockQuantity(totalRequired.intValue());
                stockRepository.save(stock);
                StockRecord record = StockRecord.create(
                        generateRecordNo(),
                        product,
                        StockRecordType.OUT,
                        StockRecordSubType.SALES,
                        -totalRequired.intValue(),
                        beforeQty,
                        stock.getAvailableQuantity(),
                        currentSysUser.id(),
                        currentSysUser.username(),
                        "销售订单锁库: " + order.getOrderNo()
                );
                stockRecordRepository.save(record);
            }
        }
    }

    private void unlockOrderItems(SalesOrder order) {
        var currentSysUser = SecurityUtils.requireCurrentSysUser();
        for (SalesOrderItem item : order.getItems()) {
            SalesSku sku = item.getSalesSku();
            for (SalesSkuComponent skuComponent : sku.getComponents()) {
                Product product = skuComponent.getProduct();
                BigDecimal quantityPerSku = skuComponent.getQuantity();
                BigDecimal totalRequired = quantityPerSku.multiply(BigDecimal.valueOf(item.getQuantity()));
                Stock stock = stockRepository.findByProductIdForUpdate(product.getId())
                        .orElseThrow(() -> BusinessException.badRequest("库存产品没有库存记录: " + product.getName()));
                int beforeQty = stock.getAvailableQuantity();
                stock.unlockQuantity(totalRequired.intValue());
                stockRepository.save(stock);
                StockRecord record = StockRecord.create(
                        generateRecordNo(),
                        product,
                        StockRecordType.ADJUST,
                        StockRecordSubType.INVENTORY,
                        totalRequired.intValue(),
                        beforeQty,
                        stock.getAvailableQuantity(),
                        currentSysUser.id(),
                        currentSysUser.username(),
                        "销售订单解锁: " + order.getOrderNo()
                );
                stockRecordRepository.save(record);
            }
        }
    }

    private String generateRecordNo() {
        return "SR" + Instant.now().toString().replace("-", "").replace(":", "").replace("T", "").replace("Z", "").substring(0, 14) + String.format("%04d", ThreadLocalRandom.current().nextInt(1000, 10000));
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
