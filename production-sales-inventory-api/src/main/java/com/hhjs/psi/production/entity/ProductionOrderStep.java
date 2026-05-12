package com.hhjs.psi.production.entity;

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

import java.time.Instant;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "production_order_step")
public class ProductionOrderStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrder productionOrder;

    @Column(name = "step_code", nullable = false, length = 64)
    private String stepCode;

    @Column(name = "step_name", nullable = false, length = 80)
    private String stepName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "allow_loss", nullable = false)
    private Boolean allowLoss = true;

    @Column(name = "completed_quantity", nullable = false)
    private Integer completedQuantity = 0;

    @Column(name = "loss_quantity", nullable = false)
    private Integer lossQuantity = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProductionOrderStep() {
    }

    public static ProductionOrderStep create(
            ProductionOrder order,
            String stepCode,
            String stepName,
            Integer sortOrder,
            Boolean allowLoss
    ) {
        ProductionOrderStep step = new ProductionOrderStep();
        step.productionOrder = order;
        step.stepCode = stepCode;
        step.stepName = stepName;
        step.sortOrder = sortOrder;
        step.allowLoss = allowLoss == null || allowLoss;
        step.completedQuantity = 0;
        step.lossQuantity = 0;
        return step;
    }

    public void complete(Integer completedQuantity, Integer lossQuantity) {
        this.completedQuantity = completedQuantity;
        this.lossQuantity = lossQuantity;
    }

    public Long getId() {
        return id;
    }

    public ProductionOrder getProductionOrder() {
        return productionOrder;
    }

    public String getStepCode() {
        return stepCode;
    }

    public String getStepName() {
        return stepName;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Boolean getAllowLoss() {
        return allowLoss;
    }

    public Integer getCompletedQuantity() {
        return completedQuantity;
    }

    public Integer getLossQuantity() {
        return lossQuantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
