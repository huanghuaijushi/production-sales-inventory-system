package com.hhjs.psi.inventory.service;

import com.hhjs.psi.auth.security.SecurityUtils;
import com.hhjs.psi.inventory.dto.BusinessFlowTrendItemResponse;
import com.hhjs.psi.inventory.dto.DashboardCategoryShareResponse;
import com.hhjs.psi.inventory.dto.DashboardChartBarResponse;
import com.hhjs.psi.inventory.dto.DashboardMetricItemResponse;
import com.hhjs.psi.inventory.dto.DashboardRankingItemResponse;
import com.hhjs.psi.inventory.dto.DashboardSummaryItemResponse;
import com.hhjs.psi.inventory.dto.DashboardWarningItemResponse;
import com.hhjs.psi.inventory.dto.InventoryDistributionItemResponse;
import com.hhjs.psi.inventory.dto.InventoryDashboardResponse;
import com.hhjs.psi.inventory.dto.InventoryValueTrendItemResponse;
import com.hhjs.psi.inventory.dto.ProfitOverviewResponse;
import com.hhjs.psi.inventory.dto.StockItemResponse;
import com.hhjs.psi.inventory.dto.StockBatchResponse;
import com.hhjs.psi.inventory.dto.StockOperationRequest;
import com.hhjs.psi.inventory.dto.StockRecordResponse;
import com.hhjs.psi.inventory.dto.StockTrendItemResponse;
import com.hhjs.psi.inventory.dto.StockUpdateRequest;
import com.hhjs.psi.inventory.dto.TodayBusinessOverviewResponse;
import com.hhjs.psi.inventory.entity.Product;
import com.hhjs.psi.inventory.entity.ProductType;
import com.hhjs.psi.inventory.entity.Stock;
import com.hhjs.psi.inventory.entity.StockBatch;
import com.hhjs.psi.inventory.entity.StockRecord;
import com.hhjs.psi.inventory.entity.StockRecordSubType;
import com.hhjs.psi.inventory.entity.StockRecordType;
import com.hhjs.psi.inventory.repository.StockBatchRepository;
import com.hhjs.psi.inventory.repository.StockRecordRepository;
import com.hhjs.psi.inventory.repository.StockRepository;
import com.hhjs.psi.production.repository.ProductionStepRecordRepository;
import com.hhjs.psi.common.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final StockBatchRepository stockBatchRepository;
    private final StockRecordRepository stockRecordRepository;
    private final ProductionStepRecordRepository productionStepRecordRepository;

    public InventoryService(
            StockRepository stockRepository,
            StockBatchRepository stockBatchRepository,
            StockRecordRepository stockRecordRepository,
            ProductionStepRecordRepository productionStepRecordRepository
    ) {
        this.stockRepository = stockRepository;
        this.stockBatchRepository = stockBatchRepository;
        this.stockRecordRepository = stockRecordRepository;
        this.productionStepRecordRepository = productionStepRecordRepository;
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

        List<InventoryDistributionItemResponse> inventoryDistribution = getInventoryDistribution(allStocks);
        TodayBusinessOverviewResponse todayOverview = getTodayBusinessOverview();
        DashboardSnapshot snapshot = buildDashboardSnapshot(allStocks, todayOverview, lowStockCount, outOfStockCount, availableStockQuantity);

        return new InventoryDashboardResponse(
                totalProducts,
                lowStockCount,
                outOfStockCount,
                availableStockQuantity,
                todayOverview,
                inventoryDistribution,
                lowStockItems,
                recentUpdates,
                snapshot.topMetrics,
                snapshot.rawMaterialBars,
                snapshot.finishedProductBars,
                snapshot.rawMaterialWarnings,
                snapshot.hotProducts,
                snapshot.rawCategoryShares,
                snapshot.finishedCategoryShares,
                snapshot.rawSummaryCards,
                snapshot.finishedSummaryCards,
                snapshot.profitOverview
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

    private DashboardSnapshot buildDashboardSnapshot(
            List<Stock> stocks,
            TodayBusinessOverviewResponse todayOverview,
            int lowStockCount,
            int outOfStockCount,
            int availableStockQuantity
    ) {
        List<Stock> rawStocks = stocks.stream()
                .filter(stock -> stock.getProduct().getType() == ProductType.RAW_MATERIAL)
                .toList();
        List<Stock> finishedStocks = stocks.stream()
                .filter(stock -> stock.getProduct().getType() == ProductType.FINISHED_PRODUCT)
                .toList();
        BigDecimal totalInventoryAmount = inventoryAmount(stocks);
        BigDecimal rawInventoryAmount = inventoryAmount(rawStocks);
        int rawLowStockCount = (int) rawStocks.stream().filter(Stock::isLowStock).count();
        int finishedLowStockCount = (int) finishedStocks.stream().filter(Stock::isLowStock).count();
        int finishedQuantity = finishedStocks.stream().mapToInt(Stock::getQuantity).sum();

        return new DashboardSnapshot(
                List.of(
                        metric("sku", "在库 SKU 数", formatNumber(stocks.size()), "所有在库商品 SKU", "库", "#2563eb", "+0%", "trend-up", percent(stocks.size(), Math.max(stocks.size(), 1)), "linear-gradient(90deg, #2563eb 0%, #60a5fa 100%)"),
                        metric("inventoryAmount", "库存总金额", formatCurrency(totalInventoryAmount), "所有库存总金额", "¥", "#7c3aed", "+0%", "trend-up", "76%", "linear-gradient(90deg, #7c3aed 0%, #a78bfa 100%)"),
                        metric("rawWarning", "原料预警 SKU", formatNumber(rawLowStockCount), "低于安全库存", "警", "#f59e0b", rawLowStockCount > 0 ? "需处理" : "正常", rawLowStockCount > 0 ? "trend-down" : "trend-up", percent(rawLowStockCount, Math.max(rawStocks.size(), 1)), "linear-gradient(90deg, #f59e0b 0%, #fcd34d 100%)"),
                        metric("finishedWarning", "成品预警 SKU", formatNumber(finishedLowStockCount), "低于安全库存", "成", "#16a34a", finishedLowStockCount > 0 ? "需处理" : "正常", finishedLowStockCount > 0 ? "trend-down" : "trend-up", percent(finishedLowStockCount, Math.max(finishedStocks.size(), 1)), "linear-gradient(90deg, #16a34a 0%, #4ade80 100%)"),
                        metric("productionInbound", "今日生产入库", formatNumber(todayOverview.inboundQuantity()), "产品入库件数", "入", "#0ea5e9", "实时", "trend-up", percent(todayOverview.inboundQuantity(), Math.max(availableStockQuantity, 1)), "linear-gradient(90deg, #0ea5e9 0%, #67e8f9 100%)"),
                        metric("salesOutbound", "今日销售出库", formatNumber(todayOverview.outboundQuantity()), "销售出库件数", "出", "#ef4444", "实时", "trend-up", percent(todayOverview.outboundQuantity(), Math.max(availableStockQuantity, 1)), "linear-gradient(90deg, #ef4444 0%, #fca5a5 100%)")
                ),
                buildRawMaterialBars(),
                buildFinishedProductBars(),
                buildRawMaterialWarnings(rawStocks),
                buildHotProducts(finishedStocks),
                buildCategoryShares(rawStocks, true),
                buildCategoryShares(finishedStocks, false),
                List.of(
                        new DashboardSummaryItemResponse("原料总库存金额", formatCurrency(rawInventoryAmount), "按成本价估算"),
                        new DashboardSummaryItemResponse("低于安全库存 SKU", formatNumber(rawLowStockCount), "需优先补货 " + formatNumber(Math.min(rawLowStockCount, 8)) + " 项"),
                        new DashboardSummaryItemResponse("近 7 天平均采购", formatCurrency(averageAmount(getInventoryValueTrend(7).stream().map(InventoryValueTrendItemResponse::rawMaterialInboundAmount).toList())), "按采购入库金额")
                ),
                List.of(
                        new DashboardSummaryItemResponse("成品总库存件数", formatNumber(finishedQuantity) + " 件", "当前在库成品"),
                        buildHotProductsRatioSummary(finishedStocks),
                        buildFinishedOutboundAverageSummary(finishedStocks)
                ),
                buildProfitOverview()
        );
    }

    private DashboardMetricItemResponse metric(String key, String title, String value, String subtitle, String icon, String accentColor, String change, String trendClass, String progress, String progressColor) {
        return new DashboardMetricItemResponse(key, title, value, subtitle, icon, accentColor, change, trendClass, progress, progressColor);
    }

    private String normalizeDistributionCategory(Product product) {
        if (product.getCategory() != null && !product.getCategory().isBlank()) {
            return product.getCategory();
        }
        return product.getType() == ProductType.RAW_MATERIAL ? "原料" : "成品";
    }

    private List<DashboardChartBarResponse> buildRawMaterialBars() {
        List<InventoryValueTrendItemResponse> amountTrendItems = getInventoryValueTrend(7);
        List<StockTrendItemResponse> quantityTrendItems = getStockTrend(7);
        Map<String, StockTrendItemResponse> quantityByLabel = quantityTrendItems.stream()
                .collect(Collectors.toMap(StockTrendItemResponse::label, item -> item, (first, ignored) -> first));
        BigDecimal maxAmount = amountTrendItems.stream()
                .flatMap(item -> java.util.stream.Stream.of(
                        item.rawMaterialInboundAmount(),
                        item.rawMaterialUsageAmount(),
                        item.totalAmount()
                ))
                .map(this::normalizeMoney)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        BigDecimal axisMax = friendlyAmountAxisMax(maxAmount);

        return amountTrendItems.stream()
                .map(item -> {
                    StockTrendItemResponse quantityItem = quantityByLabel.get(item.label());
                    return new DashboardChartBarResponse(
                            item.label(),
                            percent(item.rawMaterialInboundAmount(), axisMax),
                            percent(item.rawMaterialUsageAmount(), axisMax),
                            percent(item.totalAmount(), axisMax),
                            null,
                            null,
                            formatCurrency(item.rawMaterialInboundAmount()),
                            formatCurrency(item.rawMaterialUsageAmount()),
                            formatCurrency(item.totalAmount()),
                            null,
                            null,
                            formatNumber(quantityItem == null ? 0 : quantityItem.rawMaterialInboundQuantity()),
                            formatNumber(quantityItem == null ? 0 : quantityItem.rawMaterialOutboundQuantity()),
                            formatNumber(quantityItem == null ? 0 : quantityItem.rawMaterialInboundQuantity() + quantityItem.rawMaterialOutboundQuantity()),
                            null,
                            null,
                            null
                    );
                })
                .toList();
    }

    private List<DashboardChartBarResponse> buildFinishedProductBars() {
        List<StockTrendItemResponse> quantityTrendItems = getStockTrend(7);
        List<InventoryValueTrendItemResponse> amountTrendItems = getInventoryValueTrend(7);
        Map<String, InventoryValueTrendItemResponse> amountByLabel = amountTrendItems.stream()
                .collect(Collectors.toMap(InventoryValueTrendItemResponse::label, item -> item, (first, ignored) -> first));
        int maxQuantity = quantityTrendItems.stream()
                .flatMapToInt(item -> java.util.stream.IntStream.of(
                        item.finishedProductInboundQuantity(),
                        item.finishedProductOutboundQuantity(),
                        item.finishedProductInboundQuantity() + item.finishedProductOutboundQuantity()
                ))
                .max()
                .orElse(0);
        int axisMax = friendlyQuantityAxisMax(maxQuantity);

        return quantityTrendItems.stream()
                .map(item -> {
                    InventoryValueTrendItemResponse amountItem = amountByLabel.get(item.label());
                    BigDecimal inboundAmount = amountItem == null ? BigDecimal.ZERO : amountItem.finishedProductInboundAmount();
                    BigDecimal salesAmount = amountItem == null ? BigDecimal.ZERO : amountItem.finishedProductSalesAmount();
                    BigDecimal stockAmount = inboundAmount.add(salesAmount);
                    return new DashboardChartBarResponse(
                            item.label(),
                            null,
                            null,
                            percent(item.finishedProductInboundQuantity() + item.finishedProductOutboundQuantity(), axisMax),
                            percent(item.finishedProductInboundQuantity(), axisMax),
                            percent(item.finishedProductOutboundQuantity(), axisMax),
                            null,
                            null,
                            formatNumber(item.finishedProductInboundQuantity() + item.finishedProductOutboundQuantity()) + "件",
                            formatNumber(item.finishedProductInboundQuantity()) + "件",
                            formatNumber(item.finishedProductOutboundQuantity()) + "件",
                            null,
                            null,
                            null,
                            formatCurrency(inboundAmount),
                            formatCurrency(salesAmount),
                            formatCurrency(stockAmount)
                    );
                })
                .toList();
    }

    private ProfitOverviewResponse buildProfitOverview() {
        List<InventoryValueTrendItemResponse> trendItems = getInventoryValueTrend(7);
        BigDecimal totalRevenue = trendItems.stream()
                .map(InventoryValueTrendItemResponse::finishedProductSalesAmount)
                .map(this::normalizeMoney)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCost = trendItems.stream()
                .map(InventoryValueTrendItemResponse::finishedProductInboundAmount)
                .map(this::normalizeMoney)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grossProfit = totalRevenue.subtract(totalCost);
        long orderCount = countShippedSalesOrders(7);
        BigDecimal axisMax = friendlyAmountAxisMax(trendItems.stream()
                .flatMap(item -> java.util.stream.Stream.of(
                        item.finishedProductSalesAmount(),
                        item.finishedProductInboundAmount(),
                        item.finishedProductSalesAmount().subtract(item.finishedProductInboundAmount()).max(BigDecimal.ZERO)
                ))
                .map(this::normalizeMoney)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO));

        List<DashboardChartBarResponse> trendBars = trendItems.stream()
                .map(item -> {
                    BigDecimal revenue = normalizeMoney(item.finishedProductSalesAmount());
                    BigDecimal cost = normalizeMoney(item.finishedProductInboundAmount());
                    BigDecimal profit = revenue.subtract(cost);
                    return new DashboardChartBarResponse(
                            item.label(),
                            null,
                            null,
                            percent(profit.max(BigDecimal.ZERO), axisMax),
                            percent(revenue, axisMax),
                            percent(cost, axisMax),
                            null,
                            null,
                            formatCurrency(profit),
                            formatCurrency(revenue),
                            formatCurrency(cost),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    );
                })
                .toList();

        List<DashboardRankingItemResponse> channelRanking = buildProfitChannelRanking(7);
        return new ProfitOverviewResponse(
                formatCurrency(totalRevenue),
                formatCurrency(totalCost),
                formatCurrency(grossProfit),
                ratio(grossProfit, totalRevenue),
                formatCurrency(averageAmount(totalRevenue, orderCount)),
                formatCurrency(averageAmount(grossProfit, orderCount)),
                List.of(
                        metric("revenue", "近 7 天销售收入", formatCurrency(totalRevenue), "按已发货/已完成销售单", "收", "#2563eb", "实时", "trend-up", percent(totalRevenue, totalRevenue.max(BigDecimal.ONE)), "linear-gradient(90deg, #2563eb 0%, #60a5fa 100%)"),
                        metric("cost", "近 7 天销售成本", formatCurrency(totalCost), "按成品成本价估算", "本", "#f59e0b", "成本", "trend-down", percent(totalCost, totalRevenue.max(BigDecimal.ONE)), "linear-gradient(90deg, #f59e0b 0%, #fcd34d 100%)"),
                        metric("grossProfit", "近 7 天毛利", formatCurrency(grossProfit), "收入 - 成本", "利", "#16a34a", ratio(grossProfit, totalRevenue), grossProfit.signum() >= 0 ? "trend-up" : "trend-down", percent(grossProfit.max(BigDecimal.ZERO), totalRevenue.max(BigDecimal.ONE)), "linear-gradient(90deg, #16a34a 0%, #86efac 100%)"),
                        metric("grossMargin", "近 7 天毛利率", ratio(grossProfit, totalRevenue), "按销售收入计算", "%", "#7c3aed", formatNumber((int) orderCount) + " 单", "trend-up", ratio(grossProfit.max(BigDecimal.ZERO), totalRevenue), "linear-gradient(90deg, #7c3aed 0%, #c4b5fd 100%)")
                ),
                trendBars,
                channelRanking,
                List.of(
                        new DashboardSummaryItemResponse("成交订单", formatNumber((int) orderCount) + " 单", "已发货/已完成"),
                        new DashboardSummaryItemResponse("客单销售额", formatCurrency(averageAmount(totalRevenue, orderCount)), "销售收入 / 成交订单"),
                        new DashboardSummaryItemResponse("单均毛利", formatCurrency(averageAmount(grossProfit, orderCount)), "毛利 / 成交订单")
                )
        );
    }

    private List<DashboardRankingItemResponse> buildProfitChannelRanking(int days) {
        LocalDate endDate = LocalDate.now(BUSINESS_ZONE);
        LocalDate startDate = endDate.minusDays(days - 1L);
        var start = startDate.atStartOfDay(BUSINESS_ZONE).toInstant();
        var end = endDate.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();
        List<Object[]> rows = stockRecordRepository.findProfitChannelRows(start, end);
        BigDecimal maxRevenue = rows.stream()
                .map(row -> normalizeMoney((BigDecimal) row[1]))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);
        return java.util.stream.IntStream.range(0, Math.min(rows.size(), 5))
                .mapToObj(index -> {
                    Object[] row = rows.get(index);
                    String channelName = row[0] == null ? "未设置渠道" : (String) row[0];
                    BigDecimal revenue = normalizeMoney((BigDecimal) row[1]);
                    BigDecimal cost = normalizeMoney((BigDecimal) row[2]);
                    BigDecimal profit = revenue.subtract(cost);
                    return new DashboardRankingItemResponse(
                            index + 1,
                            channelName,
                            percent(revenue, maxRevenue),
                            formatCurrency(revenue) + " / 毛利 " + formatCurrency(profit)
                    );
                })
                .toList();
    }

    private long countShippedSalesOrders(int days) {
        LocalDate endDate = LocalDate.now(BUSINESS_ZONE);
        LocalDate startDate = endDate.minusDays(days - 1L);
        var start = startDate.atStartOfDay(BUSINESS_ZONE).toInstant();
        var end = endDate.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();
        return stockRecordRepository.findProfitChannelRows(start, end).stream()
                .mapToLong(row -> ((Number) row[3]).longValue())
                .sum();
    }

    private List<DashboardWarningItemResponse> buildRawMaterialWarnings(List<Stock> rawStocks) {
        return rawStocks.stream()
                .filter(stock -> stock.getQuantity() <= stock.getProduct().getAlertQuantity() * 2)
                .sorted(Comparator
                        .comparingDouble(this::stockSafetyRatio)
                        .thenComparing(Stock::getAvailableQuantity))
                .limit(5)
                .map(this::toWarningItem)
                .toList();
    }

    private double stockSafetyRatio(Stock stock) {
        int alertQuantity = Math.max(stock.getProduct().getAlertQuantity(), 1);
        return stock.getQuantity() * 1.0 / alertQuantity;
    }

    private DashboardWarningItemResponse toWarningItem(Stock stock) {
        Product product = stock.getProduct();
        boolean outOfStock = stock.getQuantity() <= 0;
        boolean warning = stock.getQuantity() <= product.getAlertQuantity();
        String status = outOfStock ? "缺货" : warning ? "预警" : "关注";
        String statusClass = outOfStock ? "is-danger" : warning ? "is-warn" : "is-info";
        return new DashboardWarningItemResponse(
                product.getName(),
                formatNumber(stock.getQuantity()) + product.getUnit(),
                formatNumber(product.getAlertQuantity()) + product.getUnit(),
                outOfStock ? "0 天" : "约 " + Math.max(1, stock.getQuantity() / Math.max(product.getAlertQuantity(), 1)) + " 天",
                status,
                statusClass
        );
    }

    private List<DashboardRankingItemResponse> buildHotProducts(List<Stock> finishedStocks) {
        LocalDate endDate = LocalDate.now(BUSINESS_ZONE);
        LocalDate startDate = endDate.minusDays(6);
        var start = startDate.atStartOfDay(BUSINESS_ZONE).toInstant();
        var end = endDate.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();
        List<Object[]> rows = stockRecordRepository.findSalesRankingRows(start, end);
        if (rows.isEmpty()) {
            return buildFinishedStockRanking(finishedStocks);
        }
        int max = rows.stream().mapToInt(row -> ((Number) row[1]).intValue()).max().orElse(1);
        return rows.stream()
                .limit(5)
                .map(row -> new DashboardRankingItemResponse(
                        rows.indexOf(row) + 1,
                        (String) row[0],
                        percent(((Number) row[1]).intValue(), max),
                        formatNumber(((Number) row[1]).intValue()) + " 件"
                ))
                .toList();
    }

    private List<DashboardRankingItemResponse> buildFinishedStockRanking(List<Stock> finishedStocks) {
        int max = finishedStocks.stream().mapToInt(Stock::getQuantity).max().orElse(1);
        return java.util.stream.IntStream.range(0, Math.min(finishedStocks.size(), 5))
                .mapToObj(index -> {
                    Stock stock = finishedStocks.stream()
                            .sorted(Comparator.comparingInt(Stock::getQuantity).reversed())
                            .toList()
                            .get(index);
                    return new DashboardRankingItemResponse(
                            index + 1,
                            stock.getProduct().getName(),
                            percent(stock.getQuantity(), max),
                            formatNumber(stock.getQuantity()) + stock.getProduct().getUnit()
                    );
                })
                .toList();
    }

    private List<DashboardCategoryShareResponse> buildCategoryShares(List<Stock> stocks, boolean amountMode) {
        String[] colors = {"#2563eb", "#7c3aed", "#f59e0b", "#14b8a6", "#22c55e"};
        Map<String, List<Stock>> grouped = stocks.stream()
                .collect(Collectors.groupingBy(stock -> normalizeDistributionCategory(stock.getProduct()), LinkedHashMap::new, Collectors.toList()));
        BigDecimal totalAmount = inventoryAmount(stocks);
        int totalQuantity = stocks.stream().mapToInt(Stock::getQuantity).sum();
        List<Map.Entry<String, List<Stock>>> entries = grouped.entrySet().stream()
                .sorted((left, right) -> {
                    BigDecimal leftValue = amountMode ? inventoryAmount(left.getValue()) : BigDecimal.valueOf(left.getValue().stream().mapToInt(Stock::getQuantity).sum());
                    BigDecimal rightValue = amountMode ? inventoryAmount(right.getValue()) : BigDecimal.valueOf(right.getValue().stream().mapToInt(Stock::getQuantity).sum());
                    return rightValue.compareTo(leftValue);
                })
                .limit(5)
                .toList();
        return java.util.stream.IntStream.range(0, entries.size())
                .mapToObj(index -> {
                    Map.Entry<String, List<Stock>> entry = entries.get(index);
                    BigDecimal value = amountMode ? inventoryAmount(entry.getValue()) : BigDecimal.valueOf(entry.getValue().stream().mapToInt(Stock::getQuantity).sum());
                    BigDecimal total = amountMode ? totalAmount : BigDecimal.valueOf(Math.max(totalQuantity, 1));
                    String ratio = ratio(value, total);
                    return new DashboardCategoryShareResponse(
                            entry.getKey(),
                            ratio,
                            amountMode ? formatCurrency(value) : formatNumber(value.intValue()) + " 件",
                            ratio,
                            colors[index % colors.length]
                    );
                })
                .toList();
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
        long lossRecordCount = productionStepRecordRepository.countLossRecords(start, end);

        int inboundQuantity = toPositiveInt(stockRecordRepository.sumQuantityByType(StockRecordType.IN, start, end));
        int outboundQuantity = toPositiveInt(stockRecordRepository.sumQuantityByType(StockRecordType.OUT, start, end));
        int lossQuantity = toPositiveInt(productionStepRecordRepository.sumLossQuantity(start, end));

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
            int signedQuantity = ((Number) row[2]).intValue();
            int quantity = Math.abs(signedQuantity);
            ProductType productType = (ProductType) row[4];
            StockTrendAggregate aggregate = aggregates.computeIfAbsent(recordDate, ignored -> new StockTrendAggregate());

            switch (type) {
                case IN -> {
                    if (productType == ProductType.RAW_MATERIAL) {
                        aggregate.rawMaterialInboundQuantity += quantity;
                    } else {
                        aggregate.finishedProductInboundQuantity += quantity;
                    }
                }
                case OUT -> {
                    if (productType == ProductType.RAW_MATERIAL) {
                        aggregate.rawMaterialOutboundQuantity += quantity;
                    } else {
                        aggregate.finishedProductOutboundQuantity += quantity;
                    }
                }
                case ADJUST -> aggregate.adjustQuantity += signedQuantity;
            }
        });

        productionStepRecordRepository.findLossTrendRows(start, end).forEach(row -> {
            LocalDate recordDate = ((java.time.Instant) row[0]).atZone(BUSINESS_ZONE).toLocalDate();
            int quantity = Math.abs(((Number) row[1]).intValue());
            StockTrendAggregate aggregate = aggregates.computeIfAbsent(recordDate, ignored -> new StockTrendAggregate());
            aggregate.productionLossQuantity += quantity;
        });

        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> {
                    StockTrendAggregate aggregate = aggregates.getOrDefault(date, new StockTrendAggregate());
                    int inboundQuantity = aggregate.rawMaterialInboundQuantity + aggregate.finishedProductInboundQuantity;
                    int outboundQuantity = aggregate.rawMaterialOutboundQuantity + aggregate.finishedProductOutboundQuantity;
                    return new StockTrendItemResponse(
                            date,
                            date.format(TREND_LABEL_FORMATTER),
                            inboundQuantity,
                            outboundQuantity,
                            inboundQuantity - outboundQuantity + aggregate.adjustQuantity,
                            aggregate.rawMaterialInboundQuantity,
                            aggregate.rawMaterialOutboundQuantity,
                            aggregate.finishedProductInboundQuantity,
                            aggregate.finishedProductOutboundQuantity,
                            aggregate.productionLossQuantity
                    );
                })
                .toList();
    }

    public List<BusinessFlowTrendItemResponse> getBusinessFlowTrend(int days) {
        int normalizedDays = normalizeTrendDays(days);
        LocalDate endDate = LocalDate.now(BUSINESS_ZONE);
        LocalDate startDate = endDate.minusDays(normalizedDays - 1L);
        var start = startDate.atStartOfDay(BUSINESS_ZONE).toInstant();
        var end = endDate.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();

        Map<LocalDate, BusinessFlowTrendAggregate> aggregates = new HashMap<>();
        stockRecordRepository.findTrendRows(start, end).forEach(row -> {
            LocalDate recordDate = ((java.time.Instant) row[0]).atZone(BUSINESS_ZONE).toLocalDate();
            StockRecordType type = (StockRecordType) row[1];
            StockRecordSubType subType = (StockRecordSubType) row[3];
            ProductType productType = (ProductType) row[4];
            BusinessFlowTrendAggregate aggregate = aggregates.computeIfAbsent(recordDate, ignored -> new BusinessFlowTrendAggregate());

            if (type == StockRecordType.IN && subType == StockRecordSubType.PURCHASE) {
                aggregate.purchaseInboundCount++;
            } else if (type == StockRecordType.OUT && subType == StockRecordSubType.PRODUCTION_USAGE) {
                aggregate.productionUsageCount++;
            } else if (type == StockRecordType.IN && subType == StockRecordSubType.PRODUCTION && productType == ProductType.FINISHED_PRODUCT) {
                aggregate.finishedProductInboundCount++;
            } else if (type == StockRecordType.OUT && subType == StockRecordSubType.SALES) {
                aggregate.salesOutboundCount++;
            }
        });

        productionStepRecordRepository.findLossTrendRows(start, end).forEach(row -> {
            LocalDate recordDate = ((java.time.Instant) row[0]).atZone(BUSINESS_ZONE).toLocalDate();
            BusinessFlowTrendAggregate aggregate = aggregates.computeIfAbsent(recordDate, ignored -> new BusinessFlowTrendAggregate());
            aggregate.productionLossCount++;
        });

        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> {
                    BusinessFlowTrendAggregate aggregate = aggregates.getOrDefault(date, new BusinessFlowTrendAggregate());
                    int totalCount = aggregate.purchaseInboundCount
                            + aggregate.productionUsageCount
                            + aggregate.finishedProductInboundCount
                            + aggregate.salesOutboundCount
                            + aggregate.productionLossCount;
                    return new BusinessFlowTrendItemResponse(
                            date,
                            date.format(TREND_LABEL_FORMATTER),
                            totalCount,
                            aggregate.purchaseInboundCount,
                            aggregate.productionUsageCount,
                            aggregate.finishedProductInboundCount,
                            aggregate.salesOutboundCount,
                            aggregate.productionLossCount
                    );
                })
                .toList();
    }

    public List<InventoryValueTrendItemResponse> getInventoryValueTrend(int days) {
        int normalizedDays = normalizeTrendDays(days);
        LocalDate endDate = LocalDate.now(BUSINESS_ZONE);
        LocalDate startDate = endDate.minusDays(normalizedDays - 1L);
        var start = startDate.atStartOfDay(BUSINESS_ZONE).toInstant();
        var end = endDate.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();

        Map<LocalDate, InventoryValueTrendAggregate> aggregates = new HashMap<>();
        stockRecordRepository.findTrendRows(start, end).forEach(row -> {
            LocalDate recordDate = ((java.time.Instant) row[0]).atZone(BUSINESS_ZONE).toLocalDate();
            StockRecordType type = (StockRecordType) row[1];
            int quantity = Math.abs(((Number) row[2]).intValue());
            StockRecordSubType subType = (StockRecordSubType) row[3];
            ProductType productType = (ProductType) row[4];
            BigDecimal costPrice = normalizeMoney((BigDecimal) row[5]);
            BigDecimal salePrice = normalizeMoney((BigDecimal) row[6]);
            BigDecimal recordAmount = row.length > 7 && row[7] != null ? normalizeMoney((BigDecimal) row[7]) : null;
            BigDecimal businessAmount = row.length > 8 && row[8] != null ? normalizeMoney((BigDecimal) row[8]) : null;
            BigDecimal costAmount = row.length > 9 && row[9] != null ? normalizeMoney((BigDecimal) row[9]) : null;
            InventoryValueTrendAggregate aggregate = aggregates.computeIfAbsent(recordDate, ignored -> new InventoryValueTrendAggregate());

            if (productType == ProductType.RAW_MATERIAL && type == StockRecordType.IN && subType == StockRecordSubType.PURCHASE) {
                aggregate.rawMaterialInboundAmount = aggregate.rawMaterialInboundAmount.add(fallbackAmount(costAmount, recordAmount, costPrice, quantity));
            } else if (productType == ProductType.RAW_MATERIAL && type == StockRecordType.OUT && subType == StockRecordSubType.PRODUCTION_USAGE) {
                aggregate.rawMaterialUsageAmount = aggregate.rawMaterialUsageAmount.add(fallbackAmount(costAmount, recordAmount, costPrice, quantity));
            } else if (productType == ProductType.FINISHED_PRODUCT && type == StockRecordType.IN && subType == StockRecordSubType.PRODUCTION) {
                aggregate.finishedProductInboundAmount = aggregate.finishedProductInboundAmount.add(fallbackAmount(costAmount, recordAmount, costPrice, quantity));
            } else if (productType == ProductType.FINISHED_PRODUCT && type == StockRecordType.OUT && subType == StockRecordSubType.SALES) {
                BigDecimal unitPrice = salePrice.signum() > 0 ? salePrice : costPrice;
                aggregate.finishedProductSalesAmount = aggregate.finishedProductSalesAmount.add(fallbackAmount(businessAmount, recordAmount, unitPrice, quantity));
            }
        });

        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> {
                    InventoryValueTrendAggregate aggregate = aggregates.getOrDefault(date, new InventoryValueTrendAggregate());
                    BigDecimal totalAmount = aggregate.rawMaterialInboundAmount
                            .add(aggregate.rawMaterialUsageAmount)
                            .add(aggregate.finishedProductInboundAmount)
                            .add(aggregate.finishedProductSalesAmount);
                    return new InventoryValueTrendItemResponse(
                            date,
                            date.format(TREND_LABEL_FORMATTER),
                            totalAmount,
                            aggregate.rawMaterialInboundAmount,
                            aggregate.rawMaterialUsageAmount,
                            aggregate.finishedProductInboundAmount,
                            aggregate.finishedProductSalesAmount
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

    public List<StockBatchResponse> getBatchesByProductId(Long productId) {
        return stockBatchRepository.findAvailableByProductId(productId).stream()
                .map(this::toStockBatchResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public int backfillInboundBatches() {
        int createdCount = stockBatchRepository.backfillBatchesForInboundRecords();
        stockBatchRepository.linkInboundRecordsToBackfilledBatches();
        return createdCount;
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
            if (afterQuantity < beforeQuantity) {
                throw BusinessException.badRequest("减少库存请使用新增出库，并选择具体批次记录盘亏或损耗");
            }

            StockBatch batch = receiveBatch(
                    product,
                    new StockOperationRequest(
                            product.getId(),
                            StockRecordType.IN,
                            StockRecordSubType.INVENTORY,
                            afterQuantity - beforeQuantity,
                            null,
                            null,
                            null,
                            "ADJ-" + generateRecordNo(StockRecordType.ADJUST),
                            LocalDate.now(BUSINESS_ZONE),
                            null,
                            normalizeRemark(request.remark())
                    )
            );
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
            record.setBatch(batch);
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

    @Transactional
    public StockRecordResponse outboundFromLocked(StockOperationRequest request) {
        return performStockOperation(request, StockRecordType.OUT, true);
    }

    public StockRecordResponse outboundFromLocked(StockOperationRequest request, BigDecimal businessUnitPriceSnapshot) {
        return performStockOperation(request, StockRecordType.OUT, true, businessUnitPriceSnapshot);
    }

    private StockRecordResponse performStockOperation(StockOperationRequest request, StockRecordType type) {
        return performStockOperation(request, type, false);
    }

    private StockRecordResponse performStockOperation(StockOperationRequest request, StockRecordType type, boolean consumeLockedQuantity) {
        return performStockOperation(request, type, consumeLockedQuantity, null);
    }

    private StockRecordResponse performStockOperation(StockOperationRequest request, StockRecordType type, boolean consumeLockedQuantity, BigDecimal businessUnitPriceSnapshot) {
        validateOperation(request, type);

        Stock stock = stockRepository.findByProductIdForUpdate(request.productId())
                .orElseThrow(() -> BusinessException.badRequest("Stock not found for product: " + request.productId()));
        Product product = stock.getProduct();
        if (!Boolean.TRUE.equals(product.getEnabled())) {
            throw BusinessException.badRequest("Product is disabled");
        }

        int beforeQuantity = stock.getQuantity();
        int changeAmount = type == StockRecordType.OUT || type == StockRecordType.ADJUST
                ? -Math.abs(request.quantity())
                : request.quantity();
        StockBatch batch = switch (type) {
            case IN -> receiveBatch(product, request);
            case OUT -> consumeBatch(product, request);
            case ADJUST -> null;
        };

        int afterQuantity = beforeQuantity + changeAmount;

        if (afterQuantity < 0) {
            throw BusinessException.badRequest("Insufficient stock. Available: " + stock.getAvailableQuantity());
        }
        if (type == StockRecordType.OUT && consumeLockedQuantity && stock.getLockedQuantity() < request.quantity()) {
            throw BusinessException.badRequest("Insufficient locked stock. Locked: " + stock.getLockedQuantity());
        }
        if (type == StockRecordType.OUT && !consumeLockedQuantity && stock.getAvailableQuantity() < request.quantity()) {
            throw BusinessException.badRequest("Insufficient available stock. Available: " + stock.getAvailableQuantity());
        }

        if (type == StockRecordType.OUT && consumeLockedQuantity) {
            stock.shipLockedQuantity(request.quantity());
        } else {
            stock.updateQuantity(afterQuantity);
        }
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

        if (batch != null) {
            record.setBatch(batch);
        } else if (request.batchNo() != null && request.productionDate() != null) {
            record.setBatchInfo(request.batchNo(), request.productionDate(), resolveExpiryDate(product, request.productionDate(), request.expiryDate()));
        }
        if (request.relatedOrderId() != null) {
            record.setRelatedOrder(resolveSourceType(request), request.relatedOrderId(), request.batchNo());
        }
        BigDecimal requestedBusinessUnitPrice = businessUnitPriceSnapshot != null ? businessUnitPriceSnapshot : request.businessUnitPrice();
        record.setAmountSnapshot(resolveCostUnitPrice(product, batch), resolveBusinessUnitPrice(product, type, request.subType(), requestedBusinessUnitPrice));

        stockRecordRepository.save(record);

        return toStockRecordResponse(record);
    }

    private void validateOperation(StockOperationRequest request, StockRecordType type) {
        if (request.quantity() == null || request.quantity() <= 0) {
            throw BusinessException.badRequest("Quantity must be greater than 0");
        }

        Set<StockRecordSubType> allowedSubTypes = switch (type) {
            case IN -> Set.of(StockRecordSubType.PURCHASE, StockRecordSubType.PRODUCTION, StockRecordSubType.INVENTORY);
            case OUT -> Set.of(
                    StockRecordSubType.SALES,
                    StockRecordSubType.PRODUCTION_USAGE,
                    StockRecordSubType.PRODUCTION_LOSS,
                    StockRecordSubType.PACKAGING_LOSS,
                    StockRecordSubType.SHIPPING_LOSS,
                    StockRecordSubType.INVENTORY
            );
            case ADJUST -> Set.of(StockRecordSubType.INVENTORY);
        };
        if (!allowedSubTypes.contains(request.subType())) {
            throw BusinessException.badRequest("Operation subtype does not match operation type");
        }
    }

    private StockBatch receiveBatch(Product product, StockOperationRequest request) {
        String batchNo = normalizeBatchNo(product, request.batchNo());
        LocalDate productionDate = request.productionDate() == null
                ? LocalDate.now(BUSINESS_ZONE)
                : request.productionDate();
        LocalDate expiryDate = resolveExpiryDate(product, productionDate, request.expiryDate());

        StockBatch batch = stockBatchRepository
                .findMatchingBatchForUpdate(product.getId(), batchNo, productionDate)
                .orElseGet(() -> StockBatch.create(
                        product,
                        batchNo,
                        productionDate,
                        expiryDate,
                        0,
                        normalizeFilter(request.remark())
                ));
        batch.increase(request.quantity());
        batch.setCostAndSource(
                normalizeMoney(product.getCostPrice()),
                resolveSourceType(request),
                request.relatedOrderId(),
                request.batchNo()
        );
        return stockBatchRepository.save(batch);
    }

    private StockBatch consumeBatch(Product product, StockOperationRequest request) {
        if (request.batchId() == null) {
            throw BusinessException.badRequest("请选择要出库或损耗的批次");
        }

        StockBatch batch = stockBatchRepository.findByIdForUpdate(request.batchId())
                .orElseThrow(() -> BusinessException.badRequest("批次不存在"));
        if (!batch.getProduct().getId().equals(product.getId())) {
            throw BusinessException.badRequest("批次商品和当前商品不一致");
        }
        if (batch.getAvailableQuantity() < request.quantity()) {
            throw BusinessException.badRequest("批次可用库存不足，当前可用: " + batch.getAvailableQuantity());
        }

        batch.decrease(request.quantity());
        return stockBatchRepository.save(batch);
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
                record.getBusinessUnitPrice(),
                record.getBusinessAmount(),
                record.getCostUnitPrice(),
                record.getCostAmount(),
                record.getBeforeQuantity(),
                record.getAfterQuantity(),
                record.getBatch() == null ? null : record.getBatch().getId(),
                record.getBatchNo(),
                record.getProductionDate(),
                record.getExpiryDate(),
                record.getOperatorName(),
                record.getRemark(),
                record.getCreatedAt()
        );
    }

    private StockBatchResponse toStockBatchResponse(StockBatch batch) {
        Product product = batch.getProduct();
        return new StockBatchResponse(
                batch.getId(),
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getUnit(),
                batch.getBatchNo(),
                batch.getProductionDate(),
                batch.getExpiryDate(),
                batch.getQuantity(),
                batch.getAvailableQuantity(),
                batch.getUnitCost(),
                batch.getRemark(),
                batch.getCreatedAt(),
                batch.getUpdatedAt()
        );
    }

    private String normalizeBatchNo(Product product, String batchNo) {
        if (batchNo != null && !batchNo.isBlank()) {
            return batchNo.trim();
        }
        String date = LocalDate.now(BUSINESS_ZONE).format(DateTimeFormatter.BASIC_ISO_DATE);
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return "%s-%s-%d".formatted(product.getCode(), date, random);
    }

    private LocalDate resolveExpiryDate(Product product, LocalDate productionDate, LocalDate requestExpiryDate) {
        if (requestExpiryDate != null) {
            return requestExpiryDate;
        }
        if (product.getShelfLifeDays() == null || productionDate == null) {
            return null;
        }
        return productionDate.plusDays(product.getShelfLifeDays());
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

    private BigDecimal resolveBusinessUnitPrice(Product product, StockRecordType type, StockRecordSubType subType, BigDecimal businessUnitPriceSnapshot) {
        if (businessUnitPriceSnapshot != null) {
            return normalizeMoney(businessUnitPriceSnapshot);
        }
        if (type == StockRecordType.OUT && subType == StockRecordSubType.SALES) {
            return firstPositiveMoney(product.getSalePrice(), product.getCostPrice());
        }
        return null;
    }

    private BigDecimal resolveCostUnitPrice(Product product, StockBatch batch) {
        if (batch != null && batch.getUnitCost() != null && batch.getUnitCost().signum() > 0) {
            return batch.getUnitCost();
        }
        return normalizeMoney(product.getCostPrice());
    }

    private BigDecimal firstPositiveMoney(BigDecimal... values) {
        for (BigDecimal value : values) {
            BigDecimal normalized = normalizeMoney(value);
            if (normalized.signum() > 0) {
                return normalized;
            }
        }
        return BigDecimal.ZERO;
    }

    private String resolveSourceType(StockOperationRequest request) {
        return switch (request.subType()) {
            case PURCHASE -> "PURCHASE_ORDER";
            case SALES -> "SALES_ORDER";
            case PRODUCTION, PRODUCTION_USAGE, PRODUCTION_LOSS -> "PRODUCTION_ORDER";
            case PACKAGING_LOSS, SHIPPING_LOSS, INVENTORY -> "INVENTORY_OPERATION";
        };
    }

    private String normalizeRemark(String remark) {
        return remark == null || remark.isBlank() ? "库存列表盘点调整" : remark.trim();
    }

    private int toPositiveInt(Long value) {
        return Math.toIntExact(Math.abs(value == null ? 0L : value));
    }

    private BigDecimal inventoryAmount(List<Stock> stocks) {
        return stocks.stream()
                .map(stock -> amount(stock.getProduct().getCostPrice(), stock.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String formatNumber(int value) {
        return java.text.NumberFormat.getIntegerInstance(java.util.Locale.CHINA).format(value);
    }

    private String formatCurrency(BigDecimal value) {
        return "¥" + java.text.NumberFormat.getIntegerInstance(java.util.Locale.CHINA).format(normalizeMoney(value).setScale(0, java.math.RoundingMode.HALF_UP));
    }

    private String percent(int value, int max) {
        if (max <= 0) {
            return "0%";
        }
        int result = Math.max(4, Math.min(100, Math.round(value * 100f / max)));
        return result + "%";
    }

    private String percent(BigDecimal value, BigDecimal max) {
        if (max == null || max.signum() <= 0) {
            return "0%";
        }
        int result = normalizeMoney(value)
                .multiply(BigDecimal.valueOf(100))
                .divide(max, 0, java.math.RoundingMode.HALF_UP)
                .intValue();
        return Math.max(4, Math.min(100, result)) + "%";
    }

    private String ratio(BigDecimal value, BigDecimal total) {
        if (total == null || total.signum() <= 0) {
            return "0%";
        }
        BigDecimal result = normalizeMoney(value)
                .multiply(BigDecimal.valueOf(100))
                .divide(total, 1, java.math.RoundingMode.HALF_UP);
        return result.stripTrailingZeros().toPlainString() + "%";
    }

    private BigDecimal friendlyAmountAxisMax(BigDecimal value) {
        BigDecimal normalized = normalizeMoney(value);
        if (normalized.signum() <= 0) {
            return BigDecimal.valueOf(1000);
        }
        BigDecimal[] steps = {
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(30000),
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(300000),
                BigDecimal.valueOf(500000),
                BigDecimal.valueOf(1000000)
        };
        for (BigDecimal step : steps) {
            if (normalized.compareTo(step) <= 0) {
                return step;
            }
        }
        return normalized.divide(BigDecimal.valueOf(100000), 0, java.math.RoundingMode.CEILING).multiply(BigDecimal.valueOf(100000));
    }

    private int friendlyQuantityAxisMax(int value) {
        if (value <= 0) {
            return 10;
        }
        int[] steps = {10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000};
        for (int step : steps) {
            if (value <= step) {
                return step;
            }
        }
        return ((value + 999) / 1000) * 1000;
    }

    private BigDecimal averageAmount(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = values.stream().map(this::normalizeMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(values.size()), 2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal averageAmount(BigDecimal total, long count) {
        if (count <= 0) {
            return BigDecimal.ZERO;
        }
        return normalizeMoney(total).divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP);
    }

    private int averageQuantity(List<Integer> values) {
        if (values.isEmpty()) {
            return 0;
        }
        return Math.round((float) values.stream().mapToInt(Integer::intValue).sum() / values.size());
    }

    private DashboardSummaryItemResponse buildHotProductsRatioSummary(List<Stock> finishedStocks) {
        List<Object[]> rows = stockRecordRepository.findSalesRankingRows(
                LocalDate.now(BUSINESS_ZONE).minusDays(6).atStartOfDay(BUSINESS_ZONE).toInstant(),
                LocalDate.now(BUSINESS_ZONE).plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant()
        );
        int salesTopFive = rows.stream().limit(5).mapToInt(row -> ((Number) row[1]).intValue()).sum();
        int salesTotal = rows.stream().mapToInt(row -> ((Number) row[1]).intValue()).sum();
        if (salesTotal > 0) {
            return new DashboardSummaryItemResponse(
                    "热销前五占比",
                    ratio(BigDecimal.valueOf(salesTopFive), BigDecimal.valueOf(salesTotal)),
                    "按近 7 天销售出库统计"
            );
        }

        int stockTopFive = finishedStocks.stream()
                .sorted(Comparator.comparingInt(Stock::getQuantity).reversed())
                .limit(5)
                .mapToInt(Stock::getQuantity)
                .sum();
        int stockTotal = finishedStocks.stream().mapToInt(Stock::getQuantity).sum();
        return new DashboardSummaryItemResponse(
                "成品库存前五占比",
                ratio(BigDecimal.valueOf(stockTopFive), BigDecimal.valueOf(stockTotal)),
                "暂无销售出库，按当前库存统计"
        );
    }

    private DashboardSummaryItemResponse buildFinishedOutboundAverageSummary(List<Stock> finishedStocks) {
        List<Integer> outboundQuantities = getStockTrend(7).stream()
                .map(StockTrendItemResponse::finishedProductOutboundQuantity)
                .toList();
        int outboundTotal = outboundQuantities.stream().mapToInt(Integer::intValue).sum();
        if (outboundTotal > 0) {
            return new DashboardSummaryItemResponse(
                    "近 7 天平均出库",
                    formatNumber(averageQuantity(outboundQuantities)) + " 件",
                    "按销售出库数量"
            );
        }

        int averageStock = Math.round((float) finishedStocks.stream().mapToInt(Stock::getQuantity).sum() / Math.max(finishedStocks.size(), 1));
        return new DashboardSummaryItemResponse(
                "成品平均库存",
                formatNumber(averageStock) + " 件",
                "暂无出库，按当前库存估算"
        );
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal amount(BigDecimal unitPrice, int quantity) {
        return normalizeMoney(unitPrice).multiply(BigDecimal.valueOf(quantity));
    }

    private BigDecimal fallbackAmount(BigDecimal preferredAmount, BigDecimal legacyAmount, BigDecimal fallbackUnitPrice, int quantity) {
        if (preferredAmount != null) {
            return preferredAmount;
        }
        if (legacyAmount != null) {
            return legacyAmount;
        }
        return amount(fallbackUnitPrice, quantity);
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
        private int rawMaterialInboundQuantity;
        private int rawMaterialOutboundQuantity;
        private int finishedProductInboundQuantity;
        private int finishedProductOutboundQuantity;
        private int productionLossQuantity;
        private int adjustQuantity;
    }

    private static class BusinessFlowTrendAggregate {
        private int purchaseInboundCount;
        private int productionUsageCount;
        private int finishedProductInboundCount;
        private int salesOutboundCount;
        private int productionLossCount;
    }

    private static class InventoryValueTrendAggregate {
        private BigDecimal rawMaterialInboundAmount = BigDecimal.ZERO;
        private BigDecimal rawMaterialUsageAmount = BigDecimal.ZERO;
        private BigDecimal finishedProductInboundAmount = BigDecimal.ZERO;
        private BigDecimal finishedProductSalesAmount = BigDecimal.ZERO;
    }

    private record DashboardSnapshot(
            List<DashboardMetricItemResponse> topMetrics,
            List<DashboardChartBarResponse> rawMaterialBars,
            List<DashboardChartBarResponse> finishedProductBars,
            List<DashboardWarningItemResponse> rawMaterialWarnings,
            List<DashboardRankingItemResponse> hotProducts,
            List<DashboardCategoryShareResponse> rawCategoryShares,
            List<DashboardCategoryShareResponse> finishedCategoryShares,
            List<DashboardSummaryItemResponse> rawSummaryCards,
            List<DashboardSummaryItemResponse> finishedSummaryCards,
            ProfitOverviewResponse profitOverview
    ) {
    }
}
