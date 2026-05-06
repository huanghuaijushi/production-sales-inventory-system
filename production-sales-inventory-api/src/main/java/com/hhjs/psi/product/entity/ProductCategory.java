package com.hhjs.psi.product.entity;

import com.hhjs.psi.inventory.entity.ProductType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "product_category")
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProductType type;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(length = 255)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProductCategory() {
    }

    public static ProductCategory create(String name, ProductType type, Integer sortOrder, String remark) {
        ProductCategory category = new ProductCategory();
        category.name = name;
        category.type = type;
        category.sortOrder = sortOrder == null ? 0 : sortOrder;
        category.remark = remark;
        category.enabled = true;
        return category;
    }

    public void update(String name, ProductType type, Integer sortOrder, Boolean enabled, String remark) {
        this.name = name;
        this.type = type;
        this.sortOrder = sortOrder == null ? 0 : sortOrder;
        this.enabled = enabled == null || enabled;
        this.remark = remark;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ProductType getType() {
        return type;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Boolean getEnabled() {
        return enabled;
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
