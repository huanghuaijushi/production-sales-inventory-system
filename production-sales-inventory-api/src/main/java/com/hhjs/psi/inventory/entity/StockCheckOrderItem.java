package com.hhjs.psi.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "stock_check_order_item")
public class StockCheckOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private StockCheckOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private StockBatch batch;

    @Column(name = "batch_no", nullable = false, length = 64)
    private String batchNo;

    @Column(name = "system_quantity", nullable = false)
    private Integer systemQuantity;

    @Column(name = "actual_quantity", nullable = false)
    private Integer actualQuantity;

    @Column(name = "difference_quantity", nullable = false)
    private Integer differenceQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_type", nullable = false, length = 20)
    private StockCheckResultType resultType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_record_id")
    private StockRecord stockRecord;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected StockCheckOrderItem() {
    }

    public static StockCheckOrderItem create(
            Product product,
            StockBatch batch,
            Integer systemQuantity,
            Integer actualQuantity,
            String remark
    ) {
        StockCheckOrderItem item = new StockCheckOrderItem();
        item.product = product;
        item.batch = batch;
        item.batchNo = batch.getBatchNo();
        item.systemQuantity = systemQuantity;
        item.actualQuantity = actualQuantity;
        item.differenceQuantity = actualQuantity - systemQuantity;
        item.resultType = resolveResultType(item.differenceQuantity);
        item.remark = remark;
        return item;
    }

    private static StockCheckResultType resolveResultType(Integer differenceQuantity) {
        if (differenceQuantity > 0) {
            return StockCheckResultType.GAIN;
        }
        if (differenceQuantity < 0) {
            return StockCheckResultType.LOSS;
        }
        return StockCheckResultType.MATCH;
    }

    public void attachTo(StockCheckOrder order) {
        this.order = order;
    }

    public void setStockRecord(StockRecord stockRecord) {
        this.stockRecord = stockRecord;
    }

    public Long getId() {
        return id;
    }

    public StockCheckOrder getOrder() {
        return order;
    }

    public Product getProduct() {
        return product;
    }

    public StockBatch getBatch() {
        return batch;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public Integer getSystemQuantity() {
        return systemQuantity;
    }

    public Integer getActualQuantity() {
        return actualQuantity;
    }

    public Integer getDifferenceQuantity() {
        return differenceQuantity;
    }

    public StockCheckResultType getResultType() {
        return resultType;
    }

    public StockRecord getStockRecord() {
        return stockRecord;
    }

    public String getRemark() {
        return remark;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
