package com.hhjs.psi.inventory.service;

import com.hhjs.psi.auth.security.SecurityUtils;
import com.hhjs.psi.inventory.dto.InventoryDistributionItemResponse;
import com.hhjs.psi.inventory.dto.InventoryDashboardResponse;
import com.hhjs.psi.inventory.dto.StockItemResponse;
import com.hhjs.psi.inventory.dto.StockOperationRequest;
import com.hhjs.psi.inventory.dto.StockRecordResponse;
import com.hhjs.psi.inventory.dto.StockTrendItemResponse;
import com.hhjs.psi.inventory.dto.StockUpdateRequest;
import com.hhjs.psi.inventory.dto.TodayBusinessOverviewResponse;
import com.hhjs.psi.inventory.entity.Product;
import com.hhjs.psi.inventory.entity.ProductType;
import com.hhjs.psi.inventory.entity.Stock;
import com.hhjs.psi.inventory.entity.StockRecord;
import com.hhjs.psi.inventory.entity.StockRecordSubType;
import com.hhjs.psi.inventory.entity.StockRecordType;
import com.hhjs.psi.inventory.repository.StockRecordRepository;
import com.hhjs.psi.inventory.repository.StockRepository;
import com.hhjs.psi.common.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter TREND_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MM/dd");

    private final StockRepository stockRepository;
    private final StockRecordRepository stockRecordRepository;

    public InventoryService(
            StockRepository stockRepository,
            StockRecordRepository stockRecordRepository
    ) {
        this.stockRepository = stockRepository;
        this.stockRecordRepository = stockRecordRepository;
    }

    public InventoryDashboardResponse getDashboard() {
        List<Stock> allStocks = stockRepository.findAllWithProduct();

        int totalProducts = allStocks.size();
        int lowStockCount = (int) allStocks.stream()
                .filter(stock -> stock.isLowStock() && stock.getQuantity() > 0)
                .count();
        int outOfStockCount = (int) allStocks.stream().filter(s -> s.getQuantity() == 0).count();
        int availableStockQuantity = allStocks.stream()
                .mapToInt(Stock::getAvailableQuantity)
                .sum();

        List<StockItemResponse> lowStockItems = allStocks.stream()
                .filter(Stock::isLowStock)
                .sorted(Comparator.comparing(Stock::getQuantity))
                .limit(10)
                .map(this::toStockItemResponse)
                .collect(Collectors.toList());

        List<StockItemResponse> recentUpdates = allStocks.stream()
                .sorted(Comparator.comparing(Stock::getUpdatedAt).reversed())
                .limit(10)
                .map(this::toStockItemResponse)
                .collect(Collectors.toList());

        return new InventoryDashboardResponse(
                totalProducts,
                lowStockCount,
                outOfStockCount,
                availableStockQuantity,
                getTodayBusinessOverview(),
                getInventoryDistribution(allStocks),
                lowStockItems,
                recentUpdates
        );
    }

    private List<InventoryDistributionItemResponse> getInventoryDistribution(List<Stock> stocks) {
        Map<String, List<Stock>> stocksByCategory = stocks.stream()
                .collect(Collectors.groupingBy(
                        stock -> normalizeDistributionCategory(stock.getProduct()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return stocksByCategory.entrySet().stream()
                .map(entry -> new InventoryDistributionItemResponse(
                        entry.getKey(),
                        entry.getValue().stream().mapToInt(Stock::getQuantity).sum(),
                        entry.getValue().size()
                ))
                .sorted(Comparator
                        .comparingInt(InventoryDistributionItemResponse::quantity)
                        .reversed()
                        .thenComparing(InventoryDistributionItemResponse::category))
                .collect(Collectors.toList());
    }

    private String normalizeDistributionCategory(Product product) {
        if (product.getCategory() != null && !product.getCategory().isBlank()) {
            return product.getCategory();
        }
        return product.getType() == ProductType.RAW_MATERIAL ? "原料" : "成品";
    }

    public TodayBusinessOverviewResponse getTodayBusinessOverview() {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        var start = today.atStartOfDay(BUSINESS_ZONE).toInstant();
        var end = today.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();

        long inboundRecordCount = stockRecordRepository.countByTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                StockRecordType.IN,
                start,
                end
        );
        long outboundRecordCount = stockRecordRepository.countByTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                StockRecordType.OUT,
                start,
                end
        );
        long lossRecordCount = stockRecordRepository.countByTypeAndSubTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                StockRecordType.OUT,
                StockRecordSubType.LOSS,
                start,
                end
        );

        int inboundQuantity = toPositiveInt(stockRecordRepository.sumQuantityByType(StockRecordType.IN, start, end));
        int outboundQuantity = toPositiveInt(stockRecordRepository.sumQuantityByType(StockRecordType.OUT, start, end));
        int lossQuantity = toPositiveInt(stockRecordRepository.sumQuantityByTypeAndSubType(
                StockRecordType.OUT,
                StockRecordSubType.LOSS,
                start,
                end
        ));

        return new TodayBusinessOverviewResponse(
                inboundRecordCount,
                outboundRecordCount,
                lossRecordCount,
                stockRecordRepository.countPendingSalesOrders(),
                inboundQuantity,
                outboundQuantity,
                lossQuantity
        );
    }

    public List<StockTrendItemResponse> getStockTrend(int days) {
        int normalizedDays = normalizeTrendDays(days);
        LocalDate endDate = LocalDate.now(BUSINESS_ZONE);
        LocalDate startDate = endDate.minusDays(normalizedDays - 1L);
        var start = startDate.atStartOfDay(BUSINESS_ZONE).toInstant();
        var end = endDate.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();

        Map<LocalDate, StockTrendAggregate> aggregates = new HashMap<>();
        stockRecordRepository.findTrendRows(start, end).forEach(row -> {
            LocalDate recordDate = ((java.time.Instant) row[0]).atZone(BUSINESS_ZONE).toLocalDate();
            StockRecordType type = (StockRecordType) row[1];
            int quantity = Math.abs(((Number) row[2]).intValue());
            StockTrendAggregate aggregate = aggregates.computeIfAbsent(recordDate, ignored -> new StockTrendAggregate());

            switch (type) {
                case IN -> aggregate.inboundQuantity += quantity;
                case OUT -> aggregate.outboundQuantity += quantity;
                case ADJUST -> aggregate.adjustQuantity += ((Number) row[2]).intValue();
            }
        });

        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> {
                    StockTrendAggregate aggregate = aggregates.getOrDefault(date, new StockTrendAggregate());
                    return new StockTrendItemResponse(
                            date,
                            date.format(TREND_LABEL_FORMATTER),
                            aggregate.inboundQuantity,
                            aggregate.outboundQuantity,
                            aggregate.inboundQuantity - aggregate.outboundQuantity + aggregate.adjustQuantity
                    );
                })
                .toList();
    }

    public List<StockItemResponse> getAllStocks() {
        return stockRepository.findAllWithProduct().stream()
                .map(this::toStockItemResponse)
                .collect(Collectors.toList());
    }

    public Page<StockItemResponse> getAllStocks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "product.code"));
        return stockRepository.findAllWithProduct(pageable)
                .map(this::toStockItemResponse);
    }

    public Page<StockItemResponse> getAllStocks(int page, int size, String query, String category, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "product.code"));
        String normalizedQuery = normalizeFilter(query);
        String normalizedCategory = normalizeFilter(category);
        String normalizedStatus = normalizeFilter(status);

        if ("all".equals(normalizedCategory)) {
            normalizedCategory = null;
        }
        if ("all".equals(normalizedStatus)) {
            normalizedStatus = null;
        }

        return stockRepository.searchStocks(normalizedQuery, normalizedCategory, normalizedStatus, pageable)
                .map(this::toStockItemResponse);
    }

    public StockItemResponse getStockByProductId(Long productId) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found for product: " + productId));
        return toStockItemResponse(stock);
    }

    @Transactional
    public StockItemResponse updateStock(Long stockId, StockUpdateRequest request) {
        Stock stock = stockRepository.findByIdForUpdate(stockId)
                .orElseThrow(() -> BusinessException.badRequest("Stock not found: " + stockId));
        Product product = stock.getProduct();
        if (!Boolean.TRUE.equals(product.getEnabled())) {
            throw BusinessException.badRequest("Product is disabled");
        }

        if (request.quantity() < stock.getLockedQuantity()) {
            throw BusinessException.badRequest("库存数量不能小于已锁定库存: " + stock.getLockedQuantity());
        }

        int beforeQuantity = stock.getQuantity();
        int afterQuantity = request.quantity();
        product.updateAlertQuantity(request.alertQuantity());

        if (beforeQuantity != afterQuantity) {
            stock.updateQuantity(afterQuantity);
            stockRepository.save(stock);

            var currentAdmin = SecurityUtils.requireCurrentAdmin();
            StockRecord record = StockRecord.create(
                    generateRecordNo(StockRecordType.ADJUST),
                    product,
                    StockRecordType.ADJUST,
                    StockRecordSubType.INVENTORY,
                    afterQuantity - beforeQuantity,
                    beforeQuantity,
                    afterQuantity,
                    currentAdmin.id(),
                    currentAdmin.username(),
                    normalizeRemark(request.remark())
            );
            stockRecordRepository.save(record);
        }

        return toStockItemResponse(stock);
    }

    @Transactional
    public StockRecordResponse performStockOperation(StockOperationRequest request) {
        if (request.type() == null) {
            throw BusinessException.badRequest("Operation type is required");
        }
        return performStockOperation(request, request.type());
    }

    @Transactional
    public StockRecordResponse inbound(StockOperationRequest request) {
        return performStockOperation(request, StockRecordType.IN);
    }

    @Transactional
    public StockRecordResponse outbound(StockOperationRequest request) {
        return performStockOperation(request, StockRecordType.OUT);
    }

    private StockRecordResponse performStockOperation(StockOperationRequest request, StockRecordType type) {
        validateOperation(request, type);

        Stock stock = stockRepository.findByProductIdForUpdate(request.productId())
                .orElseThrow(() -> BusinessException.badRequest("Stock not found for product: " + request.productId()));
        Product product = stock.getProduct();
        if (!Boolean.TRUE.equals(product.getEnabled())) {
            throw BusinessException.badRequest("Product is disabled");
        }

        int beforeQuantity = stock.getQuantity();
        int changeAmount = request.quantity();

        if (type == StockRecordType.OUT || type == StockRecordType.ADJUST) {
            changeAmount = -Math.abs(changeAmount);
        }

        int afterQuantity = beforeQuantity + changeAmount;

        if (afterQuantity < 0) {
            throw BusinessException.badRequest("Insufficient stock. Available: " + stock.getAvailableQuantity());
        }
        if (type == StockRecordType.OUT && stock.getAvailableQuantity() < request.quantity()) {
            throw BusinessException.badRequest("Insufficient available stock. Available: " + stock.getAvailableQuantity());
        }

        stock.updateQuantity(afterQuantity);
        stockRepository.save(stock);

        String recordNo = generateRecordNo(type);
        var currentAdmin = SecurityUtils.requireCurrentAdmin();

        StockRecord record = StockRecord.create(
                recordNo,
                product,
                type,
                request.subType(),
                changeAmount,
                beforeQuantity,
                afterQuantity,
                currentAdmin.id(),
                currentAdmin.username(),
                request.remark()
        );

        if (request.batchNo() != null && request.productionDate() != null) {
            LocalDate expiryDate = null;
            if (product.getShelfLifeDays() != null) {
                expiryDate = request.productionDate().plusDays(product.getShelfLifeDays());
            }
            record.setBatchInfo(request.batchNo(), request.productionDate(), expiryDate);
        }

        stockRecordRepository.save(record);

        return toStockRecordResponse(record);
    }

    private void validateOperation(StockOperationRequest request, StockRecordType type) {
        if (request.quantity() == null || request.quantity() <= 0) {
            throw BusinessException.badRequest("Quantity must be greater than 0");
        }

        Set<StockRecordSubType> allowedSubTypes = switch (type) {
            case IN -> Set.of(StockRecordSubType.PURCHASE, StockRecordSubType.PRODUCTION, StockRecordSubType.INVENTORY);
            case OUT -> Set.of(StockRecordSubType.SALES, StockRecordSubType.LOSS, StockRecordSubType.INVENTORY);
            case ADJUST -> Set.of(StockRecordSubType.INVENTORY);
        };
        if (!allowedSubTypes.contains(request.subType())) {
            throw BusinessException.badRequest("Operation subtype does not match operation type");
        }
    }

    public Page<StockRecordResponse> getStockRecords(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return stockRecordRepository.findAllWithProduct(pageable)
                .map(this::toStockRecordResponse);
    }

    public List<StockRecordResponse> getStockRecordsByProduct(Long productId) {
        return stockRecordRepository.findByProductId(productId).stream()
                .map(this::toStockRecordResponse)
                .collect(Collectors.toList());
    }

    private StockItemResponse toStockItemResponse(Stock stock) {
        Product product = stock.getProduct();
        return new StockItemResponse(
                stock.getId(),
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getType(),
                product.getCategory(),
                product.getSpecification(),
                product.getUnit(),
                stock.getQuantity(),
                stock.getLockedQuantity(),
                stock.getAvailableQuantity(),
                product.getAlertQuantity(),
                product.getCostPrice(),
                product.getSalePrice(),
                stock.isLowStock()
        );
    }

    private StockRecordResponse toStockRecordResponse(StockRecord record) {
        Product product = record.getProduct();
        return new StockRecordResponse(
                record.getId(),
                record.getRecordNo(),
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getUnit(),
                record.getType(),
                record.getSubType(),
                record.getQuantity(),
                record.getBeforeQuantity(),
                record.getAfterQuantity(),
                record.getBatchNo(),
                record.getProductionDate(),
                record.getExpiryDate(),
                record.getOperatorName(),
                record.getRemark(),
                record.getCreatedAt()
        );
    }

    private String generateRecordNo(StockRecordType type) {
        String prefix = switch (type) {
            case IN -> "IN";
            case OUT -> "OUT";
            case ADJUST -> "ADJ";
        };
        String timestamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "%s%s%d".formatted(prefix, timestamp, random);
    }

    private String normalizeRemark(String remark) {
        return remark == null || remark.isBlank() ? "库存列表盘点调整" : remark.trim();
    }

    private int toPositiveInt(Long value) {
        return Math.toIntExact(Math.abs(value == null ? 0L : value));
    }

    private String normalizeFilter(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private int normalizeTrendDays(int days) {
        return switch (days) {
            case 30, 90 -> days;
            default -> 7;
        };
    }

    private static class StockTrendAggregate {
        private int inboundQuantity;
        private int outboundQuantity;
        private int adjustQuantity;
    }
}
