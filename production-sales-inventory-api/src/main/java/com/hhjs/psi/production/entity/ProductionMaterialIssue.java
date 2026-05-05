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

import java.time.Instant;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "production_material_issue")
public class ProductionMaterialIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrder productionOrder;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "material_plan_id", nullable = false)
    private ProductionMaterialPlan materialPlan;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "material_product_id", nullable = false)
    private Product materialProduct;

    @Column(name = "stock_batch_id", nullable = false)
    private Long stockBatchId;

    @Column(name = "batch_no", nullable = false, length = 64)
    private String batchNo;

    @Column(name = "issued_quantity", nullable = false)
    private Integer issuedQuantity;

    @Column(name = "stock_record_id")
    private Long stockRecordId;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "operator_name", nullable = false, length = 80)
    private String operatorName;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ProductionMaterialIssue() {
    }

    public static ProductionMaterialIssue create(
            ProductionOrder productionOrder,
            ProductionMaterialPlan materialPlan,
            Product materialProduct,
            Long stockBatchId,
            String batchNo,
            Integer issuedQuantity,
            Long stockRecordId,
            Long operatorId,
            String operatorName,
            String remark
    ) {
        ProductionMaterialIssue issue = new ProductionMaterialIssue();
        issue.productionOrder = productionOrder;
        issue.materialPlan = materialPlan;
        issue.materialProduct = materialProduct;
        issue.stockBatchId = stockBatchId;
        issue.batchNo = batchNo;
        issue.issuedQuantity = issuedQuantity;
        issue.stockRecordId = stockRecordId;
        issue.operatorId = operatorId;
        issue.operatorName = operatorName;
        issue.remark = remark;
        return issue;
    }

    public Long getId() {
        return id;
    }

    public ProductionOrder getProductionOrder() {
        return productionOrder;
    }

    public ProductionMaterialPlan getMaterialPlan() {
        return materialPlan;
    }

    public Product getMaterialProduct() {
        return materialProduct;
    }

    public Long getStockBatchId() {
        return stockBatchId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public Integer getIssuedQuantity() {
        return issuedQuantity;
    }

    public Long getStockRecordId() {
        return stockRecordId;
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
