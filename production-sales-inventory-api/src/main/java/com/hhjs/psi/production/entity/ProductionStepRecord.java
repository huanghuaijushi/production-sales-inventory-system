package com.hhjs.psi.production.entity;

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

import java.time.Instant;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "production_step_record")
public class ProductionStepRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrder productionOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false, length = 30)
    private ProductionStepType stepType;

    @Column(name = "completed_quantity", nullable = false)
    private Integer completedQuantity;

    @Column(name = "loss_quantity", nullable = false)
    private Integer lossQuantity;

    @Column(name = "loss_reason", length = 500)
    private String lossReason;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "operator_name", nullable = false, length = 80)
    private String operatorName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ProductionStepRecord() {
    }

    public static ProductionStepRecord create(
            ProductionOrder productionOrder,
            ProductionStepType stepType,
            Integer completedQuantity,
            Integer lossQuantity,
            String lossReason,
            Long operatorId,
            String operatorName
    ) {
        ProductionStepRecord record = new ProductionStepRecord();
        record.productionOrder = productionOrder;
        record.stepType = stepType;
        record.completedQuantity = completedQuantity;
        record.lossQuantity = lossQuantity;
        record.lossReason = lossReason;
        record.operatorId = operatorId;
        record.operatorName = operatorName;
        return record;
    }

    public Long getId() {
        return id;
    }

    public ProductionOrder getProductionOrder() {
        return productionOrder;
    }

    public ProductionStepType getStepType() {
        return stepType;
    }

    public Integer getCompletedQuantity() {
        return completedQuantity;
    }

    public Integer getLossQuantity() {
        return lossQuantity;
    }

    public String getLossReason() {
        return lossReason;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
