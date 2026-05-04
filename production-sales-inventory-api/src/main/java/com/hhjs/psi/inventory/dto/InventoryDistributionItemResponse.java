package com.hhjs.psi.inventory.dto;

public record InventoryDistributionItemResponse(
        String category,
        int quantity,
        long productCount
) {
}
