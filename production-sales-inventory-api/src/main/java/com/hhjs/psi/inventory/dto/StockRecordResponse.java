package com.hhjs.psi.inventory.dto;

import com.hhjs.psi.inventory.entity.StockRecordSubType;
import com.hhjs.psi.inventory.entity.StockRecordType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record StockRecordResponse(
        Long id,
        String recordNo,
        Long productId,
        String productCode,
        String productName,
        String productUnit,
        StockRecordType type,
        StockRecordSubType subType,
        Integer quantity,
        BigDecimal businessUnitPrice,
        BigDecimal businessAmount,
        BigDecimal costUnitPrice,
        BigDecimal costAmount,
        Integer beforeQuantity,
        Integer afterQuantity,
        Long batchId,
        String batchNo,
        LocalDate productionDate,
        LocalDate expiryDate,
        String operatorName,
        String remark,
        Instant createdAt
) {
}
