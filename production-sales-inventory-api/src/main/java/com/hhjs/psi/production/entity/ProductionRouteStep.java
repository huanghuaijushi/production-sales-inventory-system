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
@Table(name = "production_route_step")
public class ProductionRouteStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "step_code", nullable = false, length = 64)
    private String stepCode;

    @Column(name = "step_name", nullable = false, length = 80)
    private String stepName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "allow_loss", nullable = false)
    private Boolean allowLoss = true;

    @Column(nullable = false)
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProductionRouteStep() {
    }

    public static ProductionRouteStep create(Product product, String stepCode, String stepName, Integer sortOrder, Boolean allowLoss) {
        ProductionRouteStep step = new ProductionRouteStep();
        step.product = product;
        step.stepCode = stepCode;
        step.stepName = stepName;
        step.sortOrder = sortOrder;
        step.allowLoss = allowLoss == null || allowLoss;
        step.enabled = true;
        return step;
    }

    public void update(String stepCode, String stepName, Integer sortOrder, Boolean allowLoss, Boolean enabled) {
        this.stepCode = stepCode;
        this.stepName = stepName;
        this.sortOrder = sortOrder;
        this.allowLoss = allowLoss == null || allowLoss;
        this.enabled = enabled == null || enabled;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
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

    public Boolean getEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
