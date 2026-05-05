package com.hhjs.psi.inventory.entity;

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
import java.time.LocalDate;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "stock_batch")
public class StockBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "batch_no", nullable = false, length = 64)
    private String batchNo;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 0;

    @Column(name = "unit_cost", precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "source_type", length = 30)
    private String sourceType;

    @Column(name = "source_order_id")
    private Long sourceOrderId;

    @Column(name = "source_order_no", length = 64)
    private String sourceOrderNo;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected StockBatch() {
    }

    public static StockBatch create(
            Product product,
            String batchNo,
            LocalDate productionDate,
            LocalDate expiryDate,
            Integer quantity,
            String remark
    ) {
        StockBatch batch = new StockBatch();
        batch.product = product;
        batch.batchNo = batchNo;
        batch.productionDate = productionDate;
        batch.expiryDate = expiryDate;
        batch.quantity = quantity;
        batch.availableQuantity = quantity;
        batch.remark = remark;
        return batch;
    }

    public void increase(Integer amount) {
        this.quantity += amount;
        this.availableQuantity += amount;
    }

    public void decrease(Integer amount) {
        this.quantity -= amount;
        this.availableQuantity -= amount;
    }

    public void setCostAndSource(BigDecimal unitCost, String sourceType, Long sourceOrderId, String sourceOrderNo) {
        this.unitCost = unitCost;
        this.sourceType = sourceType;
        this.sourceOrderId = sourceOrderId;
        this.sourceOrderNo = sourceOrderNo;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public LocalDate getProductionDate() {
        return productionDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Long getSourceOrderId() {
        return sourceOrderId;
    }

    public String getSourceOrderNo() {
        return sourceOrderNo;
    }

    public String getRemark() {
        return remark;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
