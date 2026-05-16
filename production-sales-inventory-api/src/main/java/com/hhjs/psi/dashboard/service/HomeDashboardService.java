package com.hhjs.psi.dashboard.service;

import com.hhjs.psi.dashboard.dto.HomeDashboardResponse;
import com.hhjs.psi.dashboard.dto.InventoryAlertsBlock;
import com.hhjs.psi.dashboard.dto.RedAlertItem;
import com.hhjs.psi.dashboard.dto.SalesMonthBlock;
import com.hhjs.psi.dashboard.dto.TodayTodosBlock;
import com.hhjs.psi.dashboard.dto.YellowAlertItem;
import com.hhjs.psi.inventory.entity.Stock;
import com.hhjs.psi.inventory.entity.StockBatch;
import com.hhjs.psi.inventory.repository.ProductRepository;
import com.hhjs.psi.inventory.repository.StockBatchRepository;
import com.hhjs.psi.inventory.repository.StockRepository;
import com.hhjs.psi.purchase.entity.PurchaseOrderStatus;
import com.hhjs.psi.purchase.repository.PurchaseOrderRepository;
import com.hhjs.psi.sales.entity.SalesOrderStatus;
import com.hhjs.psi.sales.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class HomeDashboardService {

    private static final ZoneId TIMEZONE = ZoneId.of("Asia/Shanghai");
    private static final int EXPIRY_WARNING_DAYS = 30;
    private static final int ALERT_LIST_LIMIT = 5;

    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockRepository stockRepository;
    private final StockBatchRepository stockBatchRepository;
    private final ProductRepository productRepository;

    public HomeDashboardService(
            SalesOrderRepository salesOrderRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            StockRepository stockRepository,
            StockBatchRepository stockBatchRepository,
            ProductRepository productRepository
    ) {
        this.salesOrderRepository = salesOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.stockRepository = stockRepository;
        this.stockBatchRepository = stockBatchRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public HomeDashboardResponse getHome() {
        return new HomeDashboardResponse(
                buildSalesMonth(),
                null,
                buildInventoryAlerts(),
                buildTodayTodos()
        );
    }

    private SalesMonthBlock buildSalesMonth() {
        LocalDate today = LocalDate.now(TIMEZONE);
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        LocalDate firstOfNextMonth = firstOfMonth.plusMonths(1);
        LocalDate firstOfLastMonth = firstOfMonth.minusMonths(1);

        Instant startCurrent = firstOfMonth.atStartOfDay(TIMEZONE).toInstant();
        Instant endCurrent = firstOfNextMonth.atStartOfDay(TIMEZONE).toInstant();
        Instant startLast = firstOfLastMonth.atStartOfDay(TIMEZONE).toInstant();
        Instant endLast = startCurrent;
        Instant startToday = today.atStartOfDay(TIMEZONE).toInstant();
        Instant endToday = today.plusDays(1).atStartOfDay(TIMEZONE).toInstant();

        BigDecimal current = nonNull(salesOrderRepository.sumRevenueByShipDateBetween(startCurrent, endCurrent));
        BigDecimal lastMonth = nonNull(salesOrderRepository.sumRevenueByShipDateBetween(startLast, endLast));
        BigDecimal todaySum = nonNull(salesOrderRepository.sumRevenueByShipDateBetween(startToday, endToday));

        Integer growthPercent = null;
        if (lastMonth.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = current.subtract(lastMonth);
            growthPercent = diff
                    .multiply(BigDecimal.valueOf(100))
                    .divide(lastMonth, 0, RoundingMode.HALF_UP)
                    .intValue();
        }

        return new SalesMonthBlock(current, lastMonth, growthPercent, todaySum, "CNY");
    }

    private InventoryAlertsBlock buildInventoryAlerts() {
        List<Stock> lowStock = stockRepository.findLowStockItems();
        List<RedAlertItem> redAll = lowStock.stream()
                .map(s -> new RedAlertItem(
                        s.getProduct().getId(),
                        s.getProduct().getName(),
                        s.getProduct().getType().name(),
                        s.getQuantity(),
                        s.getProduct().getAlertQuantity()
                ))
                .toList();

        LocalDate today = LocalDate.now(TIMEZONE);
        LocalDate threshold = today.plusDays(EXPIRY_WARNING_DAYS);
        List<StockBatch> expiringBatches = stockBatchRepository.findFinishedExpiringBefore(threshold);
        List<YellowAlertItem> yellowAll = expiringBatches.stream()
                .map(b -> {
                    long days = ChronoUnit.DAYS.between(today, b.getExpiryDate());
                    return new YellowAlertItem(
                            b.getProduct().getId(),
                            b.getProduct().getName(),
                            b.getBatchNo(),
                            (int) Math.max(0L, days)
                    );
                })
                .toList();

        int redCount = redAll.size();
        int yellowCount = yellowAll.size();
        long totalEnabled = productRepository.countByEnabledTrue();
        int greenCount = (int) Math.max(0L, totalEnabled - redCount - yellowCount);

        List<RedAlertItem> redTop = redAll.stream().limit(ALERT_LIST_LIMIT).toList();
        List<YellowAlertItem> yellowTop = yellowAll.stream().limit(ALERT_LIST_LIMIT).toList();

        return new InventoryAlertsBlock(redCount, yellowCount, greenCount, redTop, yellowTop);
    }

    private TodayTodosBlock buildTodayTodos() {
        long shipments = salesOrderRepository.countByStatus(SalesOrderStatus.PENDING);
        long inbounds = purchaseOrderRepository.countByStatus(PurchaseOrderStatus.PENDING_INBOUND);
        return new TodayTodosBlock((int) shipments, (int) inbounds, 0);
    }

    private BigDecimal nonNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
