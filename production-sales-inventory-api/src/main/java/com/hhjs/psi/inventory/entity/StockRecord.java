package com.hhjs.psi.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "stock_record")
public class StockRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_no", nullable = false, length = 64, unique = true)
    private String recordNo;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockRecordType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_type", length = 30)
    private StockRecordSubType subType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "cost_unit_price", precision = 12, scale = 2)
    private BigDecimal costUnitPrice;

    @Column(name = "cost_amount", precision = 14, scale = 2)
    private BigDecimal costAmount;

    @Column(name = "before_quantity", nullable = false)
    private Integer beforeQuantity;

    @Column(name = "after_quantity", nullable = false)
    private Integer afterQuantity;

    @Column(name = "related_order_id")
    private Long relatedOrderId;

    @Column(name = "related_order_type", length = 30)
    private String relatedOrderType;

    @Column(name = "related_order_no", length = 64)
    private String relatedOrderNo;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "batch_id")
    private StockBatch batch;

    @Column(name = "batch_no", length = 64)
    private String batchNo;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "operator_name", nullable = false, length = 80)
    private String operatorName;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected StockRecord() {
    }

    public static StockRecord create(
            String recordNo,
            Product product,
            StockRecordType type,
            StockRecordSubType subType,
            Integer quantity,
            Integer beforeQuantity,
            Integer afterQuantity,
            Long operatorId,
            String operatorName,
            String remark
    ) {
        StockRecord record = new StockRecord();
        record.recordNo = recordNo;
        record.product = product;
        record.type = type;
        record.subType = subType;
        record.quantity = quantity;
        record.beforeQuantity = beforeQuantity;
        record.afterQuantity = afterQuantity;
        record.operatorId = operatorId;
        record.operatorName = operatorName;
        record.remark = remark;
        return record;
    }

    public void setBatchInfo(String batchNo, LocalDate productionDate, LocalDate expiryDate) {
        this.batchNo = batchNo;
        this.productionDate = productionDate;
        this.expiryDate = expiryDate;
    }

    public void setBatch(StockBatch batch) {
        this.batch = batch;
        this.batchNo = batch.getBatchNo();
        this.productionDate = batch.getProductionDate();
        this.expiryDate = batch.getExpiryDate();
    }

    public void setRelatedOrderId(Long relatedOrderId) {
        this.relatedOrderId = relatedOrderId;
    }

    public void setRelatedOrder(String relatedOrderType, Long relatedOrderId, String relatedOrderNo) {
        this.relatedOrderType = relatedOrderType;
        this.relatedOrderId = relatedOrderId;
        this.relatedOrderNo = relatedOrderNo;
    }

    public void setAmountSnapshot(BigDecimal unitPrice) {
        BigDecimal quantityAbs = BigDecimal.valueOf(Math.abs(this.quantity));
        this.costUnitPrice = normalizeMoney(unitPrice);
        this.costAmount = this.costUnitPrice.multiply(quantityAbs);
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public Long getId() {
        return id;
    }

    public String getRecordNo() {
        return recordNo;
    }

    public Product getProduct() {
        return product;
    }

    public StockRecordType getType() {
        return type;
    }

    public StockRecordSubType getSubType() {
        return subType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getCostUnitPrice() {
        return costUnitPrice;
    }

    public BigDecimal getCostAmount() {
        return costAmount;
    }

    public Integer getBeforeQuantity() {
        return beforeQuantity;
    }

    public Integer getAfterQuantity() {
        return afterQuantity;
    }

    public Long getRelatedOrderId() {
        return relatedOrderId;
    }

    public String getRelatedOrderType() {
        return relatedOrderType;
    }

    public String getRelatedOrderNo() {
        return relatedOrderNo;
    }

    public StockBatch getBatch() {
        return batch;
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

    public Long getOperatorId() {
        return operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public String getRemark() {
        return remark;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
