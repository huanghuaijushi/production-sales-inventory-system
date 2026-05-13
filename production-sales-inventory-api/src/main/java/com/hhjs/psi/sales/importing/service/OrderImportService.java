package com.hhjs.psi.sales.importing.service;

import com.hhjs.psi.auth.repository.SysUserRepository;
import com.hhjs.psi.auth.security.SecurityUtils;
import com.hhjs.psi.common.exception.BusinessException;
import com.hhjs.psi.sales.entity.SalesChannel;
import com.hhjs.psi.sales.entity.SalesOrder;
import com.hhjs.psi.sales.entity.SalesOrderItem;
import com.hhjs.psi.sales.goods.entity.SalesSku;
import com.hhjs.psi.sales.goods.repository.SalesSkuRepository;
import com.hhjs.psi.sales.importing.dto.ApplyMappingRequest;
import com.hhjs.psi.sales.importing.dto.ExternalOrderEditRequest;
import com.hhjs.psi.sales.importing.dto.ExternalOrderItemRawResponse;
import com.hhjs.psi.sales.importing.dto.ExternalOrderRawResponse;
import com.hhjs.psi.sales.importing.dto.ImportedOrderResponse;
import com.hhjs.psi.sales.importing.dto.MatchRequest;
import com.hhjs.psi.sales.importing.dto.OrderImportBatchResponse;
import com.hhjs.psi.sales.importing.dto.OrderImportBatchResponse.SkuSummary;
import com.hhjs.psi.sales.importing.dto.PddExcelImportRequest;
import com.hhjs.psi.sales.importing.dto.TextImportRequest;
import com.hhjs.psi.sales.importing.entity.ChannelProductMapping;
import com.hhjs.psi.sales.importing.entity.ExternalOrderItemRaw;
import com.hhjs.psi.sales.importing.entity.ExternalOrderRaw;
import com.hhjs.psi.sales.importing.entity.ExternalOrderStatus;
import com.hhjs.psi.sales.importing.entity.ImportBatchStatus;
import com.hhjs.psi.sales.importing.entity.ImportSourceType;
import com.hhjs.psi.sales.importing.entity.OrderImportBatch;
import com.hhjs.psi.sales.importing.entity.SalesChannelConfig;
import com.hhjs.psi.sales.importing.repository.ChannelProductMappingRepository;
import com.hhjs.psi.sales.importing.repository.ExternalOrderItemRawRepository;
import com.hhjs.psi.sales.importing.repository.ExternalOrderRawRepository;
import com.hhjs.psi.sales.importing.repository.OrderImportBatchRepository;
import com.hhjs.psi.sales.importing.repository.SalesChannelConfigRepository;
import com.hhjs.psi.sales.service.SalesOrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderImportService {

    private static final DateTimeFormatter BATCH_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final OrderImportBatchRepository batchRepository;
    private final ExternalOrderRawRepository externalOrderRepository;
    private final ExternalOrderItemRawRepository rawItemRepository;
    private final SalesChannelConfigRepository channelRepository;
    private final ChannelProductMappingRepository mappingRepository;
    private final SalesSkuRepository salesSkuRepository;
    private final SysUserRepository sysUserRepository;
    private final SalesOrderService salesOrderService;
    private final TextOrderParser textOrderParser = new TextOrderParser();

    @Value("${app.order-import.batch-unmatch-limit:200}")
    private int batchUnmatchLimit;

    public OrderImportService(
            OrderImportBatchRepository batchRepository,
            ExternalOrderRawRepository externalOrderRepository,
            ExternalOrderItemRawRepository rawItemRepository,
            SalesChannelConfigRepository channelRepository,
            ChannelProductMappingRepository mappingRepository,
            SalesSkuRepository salesSkuRepository,
            SysUserRepository sysUserRepository,
            SalesOrderService salesOrderService
    ) {
        this.batchRepository = batchRepository;
        this.externalOrderRepository = externalOrderRepository;
        this.rawItemRepository = rawItemRepository;
        this.channelRepository = channelRepository;
        this.mappingRepository = mappingRepository;
        this.salesSkuRepository = salesSkuRepository;
        this.sysUserRepository = sysUserRepository;
        this.salesOrderService = salesOrderService;
    }

    @Transactional(readOnly = true)
    public Page<OrderImportBatchResponse> getBatches(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return batchRepository.findAll(pageable).map(batch -> OrderImportBatchResponse.from(batch, null));
    }

    @Transactional(readOnly = true)
    public OrderImportBatchResponse getBatch(Long batchId) {
        OrderImportBatch batch = findBatch(batchId);
        List<ExternalOrderRawResponse> orders = externalOrderRepository.findByBatchIdOrderByIdAsc(batchId).stream()
                .map(ExternalOrderRawResponse::from)
                .toList();
        return OrderImportBatchResponse.from(batch, orders);
    }

    @Transactional(readOnly = true)
    public ExternalOrderRawResponse getImportOrder(Long externalOrderId) {
        ExternalOrderRaw externalOrder = externalOrderRepository.findWithDetailsById(externalOrderId)
                .orElseThrow(() -> BusinessException.badRequest("导入订单不存在: " + externalOrderId));
        return ExternalOrderRawResponse.from(externalOrder);
    }

    @Transactional(readOnly = true)
    public List<ExternalOrderItemRawResponse> getUnmappedItems(Long batchId) {
        return rawItemRepository.findByOwnerBatchIdOrderByIdAsc(batchId).stream()
                .map(ExternalOrderItemRawResponse::from)
                .toList();
    }

    @Transactional
    public OrderImportBatchResponse importText(TextImportRequest request) {
        SalesChannelConfig channel = findChannel(request.channelId(), ImportSourceType.TEXT);
        OrderImportBatch batch = createBatch(channel, ImportSourceType.TEXT, null, request.rawText());
        parseTextOrders(batch, request.rawText());
        refreshBatchStats(batch);
        return getBatch(batch.getId());
    }

    @Transactional
    public OrderImportBatchResponse importPddExcelText(PddExcelImportRequest request) {
        SalesChannelConfig channel = findChannel(request.channelId(), ImportSourceType.EXCEL);
        OrderImportBatch batch = createBatch(channel, ImportSourceType.EXCEL, request.fileName(), request.rawText());
        parseTextOrders(batch, request.rawText());
        refreshBatchStats(batch);
        return getBatch(batch.getId());
    }

    @Transactional
    public OrderImportBatchResponse rematchBatch(Long batchId) {
        OrderImportBatch batch = findBatch(batchId);
        externalOrderRepository.findByBatchIdOrderByIdAsc(batchId).forEach(order -> {
            order.getItems().forEach(item -> matchItem(order.getChannel().getId(), item, true));
            order.refreshStatus();
            externalOrderRepository.save(order);
        });
        refreshBatchStats(batch);
        return getBatch(batchId);
    }

    @Transactional
    public OrderImportBatchResponse updateExternalOrder(Long batchId, Long externalOrderId, ExternalOrderEditRequest request) {
        OrderImportBatch batch = findBatch(batchId);
        ExternalOrderRaw externalOrder = externalOrderRepository.findById(externalOrderId)
                .orElseThrow(() -> BusinessException.badRequest("外部订单不存在: " + externalOrderId));
        if (!externalOrder.getBatch().getId().equals(batchId)) {
            throw BusinessException.badRequest("外部订单不属于当前导入批次");
        }
        if (externalOrder.getStatus() == ExternalOrderStatus.CONVERTED) {
            throw BusinessException.badRequest("已生成销售单的外部订单不能修改");
        }
        externalOrder.updateReceiverInfo(
                normalizeOptional(request.customerName()),
                normalizeOptional(request.customerPhone()),
                normalizeOptional(request.customerAddress()),
                normalizeOptional(request.buyerMessage()),
                normalizeOptional(request.sellerRemark())
        );
        if (request.items() == null || request.items().isEmpty()) {
            throw BusinessException.badRequest("订单明细不能为空");
        }
        List<ExternalOrderItemRaw> editedItems = new ArrayList<>();
        for (ExternalOrderEditRequest.Item itemRequest : request.items()) {
            ExternalOrderItemRaw item = findOrCreateRawItem(externalOrder, itemRequest.id());
            item.updateRaw(
                    normalizeRequired(itemRequest.externalProductName(), "外部商品名称不能为空"),
                    normalizeOptional(itemRequest.externalSpecName()),
                    normalizeOptional(itemRequest.externalSkuCode()),
                    itemRequest.externalQuantity(),
                    itemRequest.externalUnitPrice()
            );
            matchItem(externalOrder.getChannel().getId(), item, true);
            editedItems.add(item);
        }
        externalOrder.replaceItems(editedItems);
        externalOrder.refreshStatus();
        externalOrderRepository.save(externalOrder);
        refreshBatchStats(batch);
        return getBatch(batchId);
    }

    @Transactional
    public List<ExternalOrderItemRawResponse> applyMapping(ApplyMappingRequest request) {
        List<ExternalOrderItemRaw> rawItems = rawItemRepository.findByOwnerBatchIdOrderByIdAsc(request.batchId());
        if (rawItems.isEmpty()) {
            throw BusinessException.badRequest("导入批次没有可匹配的商品: " + request.batchId());
        }
        Long channelId = rawItems.get(0).getExternalOrder().getChannel().getId();
        for (ExternalOrderItemRaw item : rawItems) {
            matchItem(channelId, item, false);
        }
        rawItemRepository.saveAll(rawItems);
        return rawItemRepository.findByOwnerBatchIdOrderByIdAsc(request.batchId()).stream()
                .map(ExternalOrderItemRawResponse::from)
                .toList();
    }

    @Transactional
    public ExternalOrderItemRawResponse manualMatch(MatchRequest request) {
        SalesSku sku = salesSkuRepository.findById(request.salesSkuId())
                .orElseThrow(() -> BusinessException.badRequest("销售SKU不存在: " + request.salesSkuId()));
        ExternalOrderItemRaw item = rawItemRepository.findById(request.itemId())
                .orElseThrow(() -> BusinessException.badRequest("导入明细不存在: " + request.itemId()));
        if (item.getExternalOrder().getBatch() == null || !Objects.equals(item.getExternalOrder().getBatch().getId(), request.batchId())) {
            throw BusinessException.badRequest("导入明细不属于指定批次: " + request.batchId());
        }
        Integer saleQuantity = item.getExternalQuantity().intValue();
        item.applyMatched(null, sku, saleQuantity, "手动匹配: " + sku.getName());
        rawItemRepository.save(item);
        return ExternalOrderItemRawResponse.from(item);
    }

    @Transactional(readOnly = true)
    public OrderImportBatchResponse getBatchResult(Long batchId) {
        OrderImportBatch batch = findBatch(batchId);
        int totalItems = rawItemRepository.countByOwnerBatchId(batchId);
        int matchedItems = rawItemRepository.countByOwnerBatchIdAndMatchedSalesSkuIsNotNull(batchId);
        int unmatchedItems = totalItems - matchedItems;
        List<ExternalOrderItemRaw> rawItems = rawItemRepository.findByOwnerBatchIdOrderByIdAsc(batchId);
        List<ExternalOrderItemRawResponse> itemResponses = rawItems.stream()
                .map(ExternalOrderItemRawResponse::from)
                .toList();
        List<SkuSummary> skuSummaries = buildSkuSummaries(rawItems);
        return OrderImportBatchResponse.forMatchResult(batch, totalItems, matchedItems, unmatchedItems, skuSummaries, itemResponses);
    }

    @Transactional
    public ImportedOrderResponse confirmBatch(Long batchId) {
        OrderImportBatch batch = findBatch(batchId);
        if (batch.getStatus() == ImportBatchStatus.CONFIRMED) {
            throw BusinessException.badRequest("批次已确认过: " + batch.getBatchNo());
        }
        List<ExternalOrderRaw> readyOrders = externalOrderRepository.findByBatchIdAndStatus(batchId, ExternalOrderStatus.READY);
        if (readyOrders.isEmpty()) {
            throw BusinessException.badRequest("没有可确认生成销售单的外部订单");
        }
        validateBatchUnmatchedLimit(batchId);
        int converted = 0;
        SalesOrder lastSalesOrder = null;
        for (ExternalOrderRaw externalOrder : readyOrders) {
            if (externalOrder.getSalesOrder() != null) {
                continue;
            }
            validateReadyOrder(externalOrder);
            List<SalesOrderItem> items = externalOrder.getItems().stream()
                    .map(item -> SalesOrderItem.create(
                            item.getMatchedSalesSku(),
                            item.getSaleQuantity(),
                            resolveImportedUnitPrice(item),
                            item.getExternalProductName(),
                            item.getExternalSpecName(),
                            item.getExternalQuantity(),
                            item.getMapping()
                    ))
                    .toList();
            SalesOrder salesOrder = salesOrderService.createImportedOrder(
                    toLegacyChannel(externalOrder.getChannel().getCode()),
                    externalOrder.getChannel(),
                    externalOrder.getExternalOrderNo(),
                    batch,
                    batch.getSourceType(),
                    externalOrder.getCustomerName(),
                    externalOrder.getCustomerPhone(),
                    externalOrder.getCustomerAddress(),
                    buildRemark(externalOrder.getBuyerMessage(), externalOrder.getSellerRemark()),
                    items
            );
            externalOrder.markConverted(salesOrder);
            externalOrderRepository.save(externalOrder);
            converted++;
            lastSalesOrder = salesOrder;
        }
        batch.markConfirmed(converted);
        refreshBatchStats(batch);
        if (lastSalesOrder == null) {
            throw BusinessException.badRequest("没有可生成的销售单");
        }
        return new ImportedOrderResponse(lastSalesOrder.getId(), lastSalesOrder.getOrderNo());
    }

    private void parseTextOrders(OrderImportBatch batch, String rawText) {
        List<TextOrderParser.ParsedOrder> parsedOrders = textOrderParser.parse(rawText);
        for (TextOrderParser.ParsedOrder parsed : parsedOrders) {
            if (externalOrderRepository.existsByChannelIdAndExternalOrderNo(batch.getChannel().getId(), parsed.externalOrderNo())) {
                continue;
            }
            ExternalOrderRaw order = ExternalOrderRaw.create(
                    batch,
                    batch.getChannel(),
                    parsed.externalOrderNo(),
                    toJson(rawText),
                    parsed.customerName(),
                    parsed.customerPhone(),
                    parsed.customerAddress(),
                    parsed.buyerMessage(),
                    parsed.sellerRemark()
            );
            List<ExternalOrderItemRaw> items = new ArrayList<>();
            for (TextOrderParser.ParsedItem parsedItem : parsed.items()) {
                ExternalOrderItemRaw item = ExternalOrderItemRaw.create(
                        parsedItem.productName(),
                        parsedItem.specName(),
                        parsedItem.skuCode(),
                        parsedItem.quantity(),
                        parsedItem.unitPrice()
                );
                matchItem(batch.getChannel().getId(), item, true);
                items.add(item);
            }
            order.replaceItems(items);
            order.refreshStatus();
            externalOrderRepository.save(order);
        }
    }

    private ExternalOrderItemRaw findOrCreateRawItem(ExternalOrderRaw externalOrder, Long itemId) {
        if (itemId == null) {
            return ExternalOrderItemRaw.create("待填写商品", null, null, BigDecimal.ONE, BigDecimal.ZERO);
        }
        return externalOrder.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> BusinessException.badRequest("外部订单明细不存在: " + itemId));
    }

    private void matchItem(Long channelId, ExternalOrderItemRaw item, boolean forceRematch) {
        if (!forceRematch && item.getMatchedSalesSku() != null) {
            return;
        }
        List<ChannelProductMapping> mappings = mappingRepository.findByChannelIdAndEnabledTrueOrderByPriorityAscIdAsc(channelId);
        for (ChannelProductMapping mapping : mappings) {
            if (!mapping.matches(item.getExternalProductName(), item.getExternalSpecName(), item.getExternalSkuCode())) {
                continue;
            }
            SalesSku sku = mapping.getSalesSku();
            if (sku == null) {
                item.markUnmatched("映射规则未绑定销售SKU");
                return;
            }
            Integer saleQuantity = item.getExternalQuantity() == null ? null : item.getExternalQuantity().intValue();
            if (saleQuantity == null || saleQuantity <= 0) {
                item.markUnmatched("外部数量必须大于0");
                return;
            }
            item.applyMatched(mapping, sku, saleQuantity, "已匹配映射规则: " + sku.getName());
            return;
        }
        item.markUnmatched("未找到商品映射规则");
    }

    private void refreshBatchStats(OrderImportBatch batch) {
        List<ExternalOrderRaw> orders = externalOrderRepository.findByBatchIdOrderByIdAsc(batch.getId());
        int total = orders.size();
        int ready = (int) orders.stream().filter(order -> order.getStatus() == ExternalOrderStatus.READY).count();
        int converted = (int) orders.stream().filter(order -> order.getStatus() == ExternalOrderStatus.CONVERTED).count();
        int error = (int) orders.stream().filter(order -> order.getStatus() == ExternalOrderStatus.ERROR).count();
        batch.updateStats(total, total, ready, converted, error);
        if (converted > 0 && ready == 0) {
            batch.markConfirmed(converted);
        }
        batchRepository.save(batch);
    }

    private void validateBatchUnmatchedLimit(Long batchId) {
        int unmatched = rawItemRepository.countByOwnerBatchIdAndMatchedSalesSkuIsNull(batchId);
        if (unmatched > batchUnmatchLimit) {
            throw BusinessException.badRequest(
                    String.format("未匹配的明细(%d条)超过了限制(%d条)，请先手动匹配", unmatched, batchUnmatchLimit));
        }
    }

    private void validateReadyOrder(ExternalOrderRaw externalOrder) {
        if (externalOrder.getItems() == null || externalOrder.getItems().isEmpty()) {
            throw BusinessException.badRequest("订单\"" + externalOrder.getExternalOrderNo() + "\"没有可生成的商品明细");
        }
        for (ExternalOrderItemRaw item : externalOrder.getItems()) {
            if (item.getMatchedSalesSku() == null) {
                throw BusinessException.badRequest("订单\"" + externalOrder.getExternalOrderNo() + "\"存在未匹配商品：" + item.getExternalProductName());
            }
            if (item.getSaleQuantity() == null || item.getSaleQuantity() <= 0) {
                throw BusinessException.badRequest("订单\"" + externalOrder.getExternalOrderNo() + "\"商品数量异常：" + item.getExternalProductName());
            }
            BigDecimal unitPrice = resolveImportedUnitPrice(item);
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw BusinessException.badRequest("订单\"" + externalOrder.getExternalOrderNo() + "\"商品单价异常：" + item.getExternalProductName());
            }
        }
    }

    private BigDecimal resolveImportedUnitPrice(ExternalOrderItemRaw item) {
        BigDecimal externalPrice = item.getExternalUnitPrice();
        if (externalPrice != null && externalPrice.compareTo(BigDecimal.ZERO) > 0) {
            return externalPrice;
        }
        SalesSku sku = item.getMatchedSalesSku();
        if (sku != null && sku.getPerSkuPrice() != null && sku.getPerSkuPrice().compareTo(BigDecimal.ZERO) > 0) {
            return sku.getPerSkuPrice();
        }
        return BigDecimal.ZERO;
    }

    private OrderImportBatch createBatch(SalesChannelConfig channel, ImportSourceType sourceType, String fileName, String rawText) {
        var currentSysUser = SecurityUtils.requireCurrentSysUser();
        var operator = sysUserRepository.findById(currentSysUser.id())
                .orElseThrow(() -> BusinessException.unauthorized("当前用户不存在"));
        return batchRepository.save(OrderImportBatch.create(generateBatchNo(), channel, sourceType, fileName, rawText, operator, currentSysUser.username()));
    }

    private OrderImportBatch findBatch(Long batchId) {
        return batchRepository.findWithChannelById(batchId)
                .orElseThrow(() -> BusinessException.badRequest("导入批次不存在: " + batchId));
    }

    private SalesChannelConfig findChannel(Long channelId, ImportSourceType expectedSourceType) {
        SalesChannelConfig channel = channelRepository.findById(channelId)
                .orElseThrow(() -> BusinessException.badRequest("销售渠道不存在: " + channelId));
        if (!Boolean.TRUE.equals(channel.getEnabled())) {
            throw BusinessException.badRequest("销售渠道已停用: " + channel.getName());
        }
        if (expectedSourceType != null && channel.getSourceType() != expectedSourceType) {
            throw BusinessException.badRequest("请选择来源类型为\"" + sourceTypeText(expectedSourceType) + "\"的销售渠道: " + channel.getName());
        }
        return channel;
    }

    private String sourceTypeText(ImportSourceType sourceType) {
        return switch (sourceType) {
            case EXCEL -> "Excel";
            case TEXT -> "文本";
            case MANUAL -> "手工";
            case CONTRACT -> "合同";
            case API -> "API";
        };
    }

    private SalesChannel toLegacyChannel(String code) {
        if (code == null || code.isBlank()) {
            return SalesChannel.OFFLINE;
        }
        return switch (code.trim().toUpperCase()) {
            case "DOUYIN" -> SalesChannel.DOUYIN;
            case "PINDUODUO" -> SalesChannel.PINDUODUO;
            case "WECHAT", "WECHAT_GROUP" -> SalesChannel.WECHAT_GROUP;
            case "CONTRACT" -> SalesChannel.CONTRACT;
            default -> SalesChannel.OFFLINE;
        };
    }

    private String buildRemark(String buyerMessage, String sellerRemark) {
        List<String> remarks = new ArrayList<>();
        if (buyerMessage != null && !buyerMessage.isBlank()) {
            remarks.add(buyerMessage.trim());
        }
        if (sellerRemark != null && !sellerRemark.isBlank()) {
            remarks.add(sellerRemark.trim());
        }
        return remarks.isEmpty() ? null : String.join("；", remarks);
    }

    private List<SkuSummary> buildSkuSummaries(List<ExternalOrderItemRaw> rawItems) {
        java.util.Map<Long, java.util.Map<String, Object>> skuMap = new LinkedHashMap<>();
        for (ExternalOrderItemRaw item : rawItems) {
            if (item.getMatchedSalesSku() == null) continue;
            SalesSku sku = item.getMatchedSalesSku();
            skuMap.compute(sku.getId(), (id, data) -> {
                if (data == null) {
                    data = new java.util.HashMap<>();
                    data.put("skuName", sku.getName());
                    data.put("skuCode", sku.getCode());
                    data.put("skuSpecName", sku.getSpecName());
                    data.put("unit", sku.getUnit());
                    data.put("totalQuantity", 0);
                    data.put("totalAmount", BigDecimal.ZERO);
                }
                Integer saleQuantity = item.getSaleQuantity() == null ? 0 : item.getSaleQuantity();
                data.put("totalQuantity", (int) data.get("totalQuantity") + saleQuantity);
                BigDecimal unitPrice = resolveImportedUnitPrice(item);
                if (unitPrice != null) {
                    BigDecimal itemAmount = unitPrice.multiply(BigDecimal.valueOf(saleQuantity));
                    data.put("totalAmount", ((BigDecimal) data.get("totalAmount")).add(itemAmount));
                }
                return data;
            });
        }
        List<SkuSummary> list = new ArrayList<>();
        for (java.util.Map.Entry<Long, java.util.Map<String, Object>> entry : skuMap.entrySet()) {
            java.util.Map<String, Object> data = entry.getValue();
            list.add(new SkuSummary(
                    entry.getKey(),
                    (String) data.get("skuName"),
                    (String) data.get("skuCode"),
                    (String) data.get("skuSpecName"),
                    (String) data.get("unit"),
                    (int) data.get("totalQuantity"),
                    (BigDecimal) data.get("totalAmount")
            ));
        }
        return list;
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw BusinessException.badRequest(message);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String toJson(String text) {
        return "{\"text\":\"" + text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"}";
    }

    private String generateBatchNo() {
        return "IB" + LocalDateTime.now().format(BATCH_NO_FORMATTER) + ThreadLocalRandom.current().nextInt(1000, 10000);
    }
}
