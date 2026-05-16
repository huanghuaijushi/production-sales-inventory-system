package com.hhjs.psi.dashboard.dto;

/**
 * Phase 2 占位：应收账款 + 现金跑道。
 * 当前后端不返回此块（HomeDashboardResponse 中为 null），
 * 前端按 null 展示"开发中"卡片。等应收 / 现金模块上线后再填充。
 */
public record ReceivablesAndCashBlock(
        String status
) {
}
