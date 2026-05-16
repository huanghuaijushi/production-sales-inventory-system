package com.hhjs.psi.dashboard.dto;

import java.math.BigDecimal;

public record SalesMonthBlock(
        BigDecimal currentMonthAmount,
        BigDecimal lastMonthAmount,
        Integer growthPercent,
        BigDecimal todayAmount,
        String currency
) {
}
