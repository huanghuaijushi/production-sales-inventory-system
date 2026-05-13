package com.hhjs.psi.inventory.entity;

public enum ProductType {
    FINISHED_PRODUCT,
    RAW_MATERIAL,
    PACKAGING_MATERIAL,
    SEMI_FINISHED_PRODUCT;

    public boolean isMaterial() {
        return this == RAW_MATERIAL || this == PACKAGING_MATERIAL;
    }

    public boolean isProducible() {
        return this == FINISHED_PRODUCT || this == SEMI_FINISHED_PRODUCT;
    }
}
