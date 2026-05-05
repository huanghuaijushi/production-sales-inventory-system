package com.hhjs.psi.production.entity;

import com.hhjs.psi.inventory.entity.Product;
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
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "production_order")
public class ProductionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, length = 64, unique = true)
    private String orderNo;

    @Column(name = "batch_no", nullable = false, length = 64)
    private String batchNo;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "planned_quantity", nullable = false)
    private Integer plannedQuantity;

    @Column(name = "completed_quantity", nullable = false)
    private Integer completedQuantity = 0;

    @Column(name = "inbound_quantity", nullable = false)
    private Integer inboundQuantity = 0;

    @Column(name = "loss_quantity", nullable = false)
    private Integer lossQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", length = 30)
    private ProductionStepType currentStep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductionOrderStatus status = ProductionOrderStatus.PLANNED;

    @Column(name = "planned_date")
    private LocalDate plannedDate;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "operator_name", nullable = false, length = 80)
    private String operatorName;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProductionOrder() {
    }

    public static ProductionOrder create(
            String orderNo,
            String batchNo,
            Product product,
            Integer plannedQuantity,
            LocalDate plannedDate,
            Long operatorId,
            String operatorName,
            String remark
    ) {
        ProductionOrder order = new ProductionOrder();
        order.orderNo = orderNo;
        order.batchNo = batchNo;
        order.product = product;
        order.plannedQuantity = plannedQuantity;
        order.plannedDate = plannedDate;
        order.operatorId = operatorId;
        order.operatorName = operatorName;
        order.remark = remark;
        return order;
    }

    public void start() {
        this.status = ProductionOrderStatus.IN_PROGRESS;
        if (this.currentStep == null) {
            this.currentStep = ProductionStepType.PREPARATION;
        }
        if (this.startedAt == null) {
            this.startedAt = Instant.now();
        }
    }

    public void completeStep(
            Integer completedQuantity,
            Integer lossQuantity,
            ProductionStepType nextStep
    ) {
        this.completedQuantity = completedQuantity;
        this.lossQuantity += lossQuantity;
        this.currentStep = nextStep;
        if (nextStep == null && this.status != ProductionOrderStatus.COMPLETED) {
            this.status = ProductionOrderStatus.WAIT_INBOUND;
        } else {
            this.status = ProductionOrderStatus.IN_PROGRESS;
        }
    }

    public void addInbound(Integer quantity) {
        this.inboundQuantity += quantity;
        this.completedQuantity = Math.max(this.completedQuantity, this.inboundQuantity);
        int expectedInbound = Math.max(this.plannedQuantity - this.lossQuantity, 0);
        if (this.inboundQuantity >= expectedInbound) {
            this.status = ProductionOrderStatus.COMPLETED;
            this.completedAt = Instant.now();
        } else {
            this.status = ProductionOrderStatus.WAIT_INBOUND;
        }
    }

    public void cancel() {
        this.status = ProductionOrderStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public Product getProduct() {
        return product;
    }

    public Integer getPlannedQuantity() {
        return plannedQuantity;
    }

    public Integer getCompletedQuantity() {
        return completedQuantity;
    }

    public Integer getInboundQuantity() {
        return inboundQuantity;
    }

    public Integer getLossQuantity() {
        return lossQuantity;
    }

    public ProductionStepType getCurrentStep() {
        return currentStep;
    }

    public ProductionOrderStatus getStatus() {
        return status;
    }

    public LocalDate getPlannedDate() {
        return plannedDate;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
