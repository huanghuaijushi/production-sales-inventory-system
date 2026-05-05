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

import java.time.Instant;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "production_material_plan")
public class ProductionMaterialPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrder productionOrder;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "material_product_id", nullable = false)
    private Product materialProduct;

    @Column(name = "required_quantity", nullable = false)
    private Integer requiredQuantity;

    @Column(name = "issued_quantity", nullable = false)
    private Integer issuedQuantity = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProductionMaterialPlan() {
    }

    public static ProductionMaterialPlan create(
            ProductionOrder productionOrder,
            Product materialProduct,
            Integer requiredQuantity
    ) {
        ProductionMaterialPlan plan = new ProductionMaterialPlan();
        plan.productionOrder = productionOrder;
        plan.materialProduct = materialProduct;
        plan.requiredQuantity = requiredQuantity;
        return plan;
    }

    public void addIssued(Integer quantity) {
        this.issuedQuantity += quantity;
    }

    public Long getId() {
        return id;
    }

    public ProductionOrder getProductionOrder() {
        return productionOrder;
    }

    public Product getMaterialProduct() {
        return materialProduct;
    }

    public Integer getRequiredQuantity() {
        return requiredQuantity;
    }

    public Integer getIssuedQuantity() {
        return issuedQuantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
