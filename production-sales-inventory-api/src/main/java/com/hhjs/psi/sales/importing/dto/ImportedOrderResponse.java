package com.hhjs.psi.sales.importing.dto;

public record ImportedOrderResponse(
        Long salesOrderId,
        String orderNo
) {}
