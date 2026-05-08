package com.hhjs.psi.sales.importing.service;

import com.hhjs.psi.auth.repository.SysUserRepository;
import com.hhjs.psi.auth.security.SecurityUtils;
import com.hhjs.psi.common.exception.BusinessException;
import com.hhjs.psi.sales.entity.SalesChannel;
import com.hhjs.psi.sales.entity.SalesOrder;
import com.hhjs.psi.sales.entity.SalesOrderItem;
import com.hhjs.psi.sales.importing.dto.ExternalOrderEditRequest;
import com.hhjs.psi.sales.importing.dto.ExternalOrderRawResponse;
import com.hhjs.psi.sales.importing.dto.OrderImportBatchResponse;
import com.hhjs.psi.sales.importing.dto.PddExcelImportRequest;
import com.hhjs.psi.sales.importing.dto.TextImportRequest;
import com.hhjs.psi.sales.importing.entity.ChannelProductMapping;
import com.hhjs.psi.sales.importing.entity.ExternalOrderItemRaw;
import com.hhjs.psi.sales.importing.entity.ExternalOrderStatus;
import com.hhjs.psi.sales.importing.entity.ImportBatchStatus;
import com.hhjs.psi.sales.importing.entity.ImportSourceType;
import com.hhjs.psi.sales.importing.entity.OrderImportBatch;
import com.hhjs.psi.sales.importing.entity.SalesChannelConfig;
import com.hhjs.psi.sales.importing.repository.ChannelProductMappingRepository;
import com.hhjs.psi.sales.importing.repository.ExternalOrderRawRepository;
import com.hhjs.psi.sales.importing.repository.OrderImportBatchRepository;
import com.hhjs.psi.sales.importing.repository.SalesChannelConfigRepository;
import com.hhjs.psi.sales.service.SalesOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderImportService {

    private static final DateTimeFormatter BATCH_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final OrderImportBatchRepository batchRepository;
    private final ExternalOrderRawRepository externalOrderRepository;
    private final SalesChannelConfigRepository channelRepository;
    private final ChannelProductMappingRepository mappingRepository;
    private final SysUserRepository sysUserRepository;
    private final SalesOrderService salesOrderService;
    private final TextOrderParser textOrderParser = new TextOrderParser();

    public OrderImportService(
            OrderImportBatchRepository batchRepository,
            ExternalOrderRawRepository externalOrderRepository,
            SalesChannelConfigRepository channelRepository,
            ChannelProductMappingRepository mappingRepository,
            SysUserRepository sysUserRepository,
            SalesOrderService salesOrderService
    ) {
        this.batchRepository = batchRepository;
        this.externalOrderRepository = externalOrderRepository;
        this.channelRepository = channelRepository;
        this.mappingRepository = mappingRepository;
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
            order.getItems().forEach(item -> applyMapping(order.getChannel().getId(), item));
            order.refreshStatus();
            externalOrderRepository.save(order);
        });
        refreshBatchStats(batch);
        return getBatch(batchId);
    }

    @Transactional
    public OrderImportBatchResponse updateExternalOrder(Long batchId, Long externalOrderId, ExternalOrderEditRequest request) {
        OrderImportBatch batch = findBatch(batchId);
        var externalOrder = externalOrderRepository.findById(externalOrderId)
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
            applyMapping(externalOrder.getChannel().getId(), item);
            editedItems.add(item);
        }
        externalOrder.replaceItems(editedItems);
        externalOrder.refreshStatus();
        externalOrderRepository.save(externalOrder);
        refreshBatchStats(batch);
        return getBatch(batchId);
    }

    @Transactional
    public OrderImportBatchResponse confirmBatch(Long batchId) {
        try {
            OrderImportBatch batch = findBatch(batchId);
            if (batch.getStatus() == ImportBatchStatus.CONFIRMED) {
                return getBatch(batchId);
            }
            List<com.hhjs.psi.sales.importing.entity.ExternalOrderRaw> readyOrders = externalOrderRepository.findByBatchIdAndStatus(batchId, ExternalOrderStatus.READY);
            if (readyOrders.isEmpty()) {
                throw BusinessException.badRequest("没有可确认生成销售单的外部订单");
            }
            int converted = 0;
            for (var externalOrder : readyOrders) {
                if (externalOrder.getSalesOrder() != null) {
                    continue;
                }
                validateReadyOrder(externalOrder);
                List<SalesOrderItem> items = externalOrder.getItems().stream()
                        .map(item -> SalesOrderItem.create(
                                item.getMatchedProduct(),
                                item.getConvertedQuantity(),
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
            }
            batch.markConfirmed(converted);
            refreshBatchStats(batch);
            return getBatch(batchId);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw BusinessException.badRequest("确认生成销售单失败：" + rootCauseMessage(exception));
        }
    }

    private void parseTextOrders(OrderImportBatch batch, String rawText) {
        List<TextOrderParser.ParsedOrder> parsedOrders = textOrderParser.parse(rawText);
        for (TextOrderParser.ParsedOrder parsed : parsedOrders) {
            if (externalOrderRepository.existsByChannelIdAndExternalOrderNo(batch.getChannel().getId(), parsed.externalOrderNo())) {
                continue;
            }
            var order = com.hhjs.psi.sales.importing.entity.ExternalOrderRaw.create(
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
                applyMapping(batch.getChannel().getId(), item);
                items.add(item);
            }
            order.replaceItems(items);
            order.refreshStatus();
            externalOrderRepository.save(order);
        }
    }

    private ExternalOrderItemRaw findOrCreateRawItem(com.hhjs.psi.sales.importing.entity.ExternalOrderRaw externalOrder, Long itemId) {
        if (itemId == null) {
            return ExternalOrderItemRaw.create("待填写商品", null, null, java.math.BigDecimal.ONE, java.math.BigDecimal.ZERO);
        }
        return externalOrder.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> BusinessException.badRequest("外部订单明细不存在: " + itemId));
    }

    private void applyMapping(Long channelId, ExternalOrderItemRaw item) {
        List<ChannelProductMapping> mappings = mappingRepository.findByChannelIdAndEnabledTrueOrderByPriorityAscIdAsc(channelId);
        for (ChannelProductMapping mapping : mappings) {
            if (mapping.matches(item.getExternalProductName(), item.getExternalSpecName(), item.getExternalSkuCode())) {
                int convertedQuantity = item.getExternalQuantity().multiply(mapping.getQuantityMultiplier()).setScale(0, RoundingMode.HALF_UP).intValue();
                if (convertedQuantity <= 0) {
                    item.markUnmatched("换算后的商品数量必须大于0");
                    return;
                }
                item.applyMatched(mapping, mapping.getProduct(), convertedQuantity, "已匹配商品映射规则");
                return;
            }
        }
        item.markUnmatched("未找到商品映射规则");
    }

    private void refreshBatchStats(OrderImportBatch batch) {
        List<com.hhjs.psi.sales.importing.entity.ExternalOrderRaw> orders = externalOrderRepository.findByBatchIdOrderByIdAsc(batch.getId());
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

    private void validateReadyOrder(com.hhjs.psi.sales.importing.entity.ExternalOrderRaw externalOrder) {
        if (externalOrder.getItems() == null || externalOrder.getItems().isEmpty()) {
            throw BusinessException.badRequest("订单“" + externalOrder.getExternalOrderNo() + "”没有可生成的商品明细");
        }
        for (ExternalOrderItemRaw item : externalOrder.getItems()) {
            if (item.getMatchedProduct() == null) {
                throw BusinessException.badRequest("订单“" + externalOrder.getExternalOrderNo() + "”存在未匹配商品：" + item.getExternalProductName());
            }
            if (item.getConvertedQuantity() == null || item.getConvertedQuantity() <= 0) {
                throw BusinessException.badRequest("订单“" + externalOrder.getExternalOrderNo() + "”商品数量异常：" + item.getExternalProductName());
            }
            if (resolveImportedUnitPrice(item) == null) {
                throw BusinessException.badRequest("订单“" + externalOrder.getExternalOrderNo() + "”商品单价异常：" + item.getExternalProductName());
            }
        }
    }

    private java.math.BigDecimal resolveImportedUnitPrice(ExternalOrderItemRaw item) {
        if (item.getExternalUnitPrice() != null && item.getExternalUnitPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
            return item.getExternalUnitPrice();
        }
        if (item.getMapping() != null && item.getMapping().getDefaultUnitPrice() != null && item.getConvertedQuantity() != null && item.getConvertedQuantity() > 0) {
            return item.getMapping().getDefaultUnitPrice().divide(java.math.BigDecimal.valueOf(item.getConvertedQuantity()), 2, RoundingMode.HALF_UP);
        }
        if (item.getMatchedProduct() != null && item.getMatchedProduct().getSalePrice() != null) {
            return item.getMatchedProduct().getSalePrice();
        }
        return java.math.BigDecimal.ZERO;
    }

    private OrderImportBatch createBatch(SalesChannelConfig channel, ImportSourceType sourceType, String fileName, String rawText) {
        var currentSysUser = SecurityUtils.requireCurrentSysUser();
        var operator = sysUserRepository.findById(currentSysUser.id()).orElseThrow(() -> BusinessException.unauthorized("当前用户不存在"));
        return batchRepository.save(OrderImportBatch.create(generateBatchNo(), channel, sourceType, fileName, rawText, operator, currentSysUser.username()));
    }

    private OrderImportBatch findBatch(Long batchId) {
        return batchRepository.findWithChannelById(batchId).orElseThrow(() -> BusinessException.badRequest("导入批次不存在: " + batchId));
    }

    private SalesChannelConfig findChannel(Long channelId, ImportSourceType expectedSourceType) {
        SalesChannelConfig channel = channelRepository.findById(channelId).orElseThrow(() -> BusinessException.badRequest("销售渠道不存在: " + channelId));
        if (!Boolean.TRUE.equals(channel.getEnabled())) {
            throw BusinessException.badRequest("销售渠道已停用: " + channel.getName());
        }
        if (expectedSourceType != null && channel.getSourceType() != expectedSourceType) {
            throw BusinessException.badRequest("请选择来源类型为“" + sourceTypeText(expectedSourceType) + "”的销售渠道: " + channel.getName());
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
            case "OFFLINE", "WECHAT_GROUP", "CONTRACT" -> SalesChannel.OFFLINE;
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

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        String message = null;
        while (current != null) {
            if (current.getMessage() != null && !current.getMessage().isBlank()) {
                message = current.getMessage();
            }
            current = current.getCause();
        }
        return message == null || message.isBlank() ? throwable.getClass().getSimpleName() : message;
    }

    private String toJson(String text) {
        return "{\"text\":\"" + text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"}";
    }

    private String generateBatchNo() {
        return "IB" + LocalDateTime.now().format(BATCH_NO_FORMATTER) + ThreadLocalRandom.current().nextInt(1000, 10000);
    }
}
