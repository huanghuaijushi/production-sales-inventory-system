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
import com.hhjs.psi.product.entity.ProductCategory;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @Column(length = 64)
    private String specification;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "alert_quantity", nullable = false)
    private Integer alertQuantity = 0;

    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Product() {
    }

    public static Product create(
            String code,
            String name,
            ProductType type,
            ProductCategory category,
            String specification,
            String unit,
            BigDecimal costPrice,
            Integer alertQuantity,
            String description
    ) {
        Product product = new Product();
        product.code = code;
        product.name = name;
        product.type = type;
        product.category = category;
        product.specification = specification;
        product.unit = unit;
        product.costPrice = costPrice;
        product.alertQuantity = alertQuantity == null ? 0 : alertQuantity;
        product.description = description;
        product.enabled = true;
        return product;
    }

    public void updateBasicInfo(
            String code,
            String name,
            ProductType type,
            ProductCategory category,
            String specification,
            String unit,
            BigDecimal costPrice,
            Integer alertQuantity,
            String description
    ) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.category = category;
        this.specification = specification;
        this.unit = unit;
        this.costPrice = costPrice;
        this.alertQuantity = alertQuantity == null ? 0 : alertQuantity;
        this.description = description;
    }

    public void updateAlertQuantity(Integer alertQuantity) {
        this.alertQuantity = alertQuantity == null ? 0 : alertQuantity;
    }

    public void disable() {
        this.enabled = false;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public ProductType getType() {
        return type;
    }

    public String getCategory() {
        return category == null ? null : category.getName();
    }

    public Long getCategoryId() {
        return category == null ? null : category.getId();
    }

    public ProductCategory getCategoryEntity() {
        return category;
    }

    public String getSpecification() {
        return specification;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public Integer getAlertQuantity() {
        return alertQuantity;
    }

    public Integer getShelfLifeDays() {
        return shelfLifeDays;
    }

    public String getDescription() {
        return description;
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
