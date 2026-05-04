package com.hhjs.psi.production.entity;

import com.hhjs.psi.inventory.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "bom_item")
public class BomItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "finished_product_id", nullable = false)
    private Product finishedProduct;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "material_product_id", nullable = false)
    private Product materialProduct;

    @Column(name = "quantity_per_unit", nullable = false, precision = 12, scale = 4)
    private BigDecimal quantityPerUnit;

    @Column(name = "loss_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal lossRate = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BomItem() {
    }

    public static BomItem create(
            Product finishedProduct,
            Product materialProduct,
            BigDecimal quantityPerUnit,
            BigDecimal lossRate
    ) {
        BomItem item = new BomItem();
        item.finishedProduct = finishedProduct;
        item.materialProduct = materialProduct;
        item.quantityPerUnit = quantityPerUnit;
        item.lossRate = lossRate == null ? BigDecimal.ZERO : lossRate;
        return item;
    }

    public void update(
            Product finishedProduct,
            Product materialProduct,
            BigDecimal quantityPerUnit,
            BigDecimal lossRate
    ) {
        this.finishedProduct = finishedProduct;
        this.materialProduct = materialProduct;
        this.quantityPerUnit = quantityPerUnit;
        this.lossRate = lossRate == null ? BigDecimal.ZERO : lossRate;
    }

    public Long getId() {
        return id;
    }

    public Product getFinishedProduct() {
        return finishedProduct;
    }

    public Product getMaterialProduct() {
        return materialProduct;
    }

    public BigDecimal getQuantityPerUnit() {
        return quantityPerUnit;
    }

    public BigDecimal getLossRate() {
        return lossRate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
