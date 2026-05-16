package com.hhjs.psi.dashboard.dto;

public record TodayTodosBlock(
        Integer pendingShipments,
        Integer pendingInbounds,
        Integer pendingApprovals
) {
}
