package com.hhjs.psi.production.entity;

import com.hhjs.psi.inventory.entity.Product;
import com.hhjs.psi.supplier.entity.Supplier;
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

import java.math.BigDecimal;
import java.time.Instant;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "supplier_material")
public class SupplierMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "default_unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal defaultUnitPrice = BigDecimal.ZERO;

    @Column(name = "min_order_quantity", nullable = false)
    private Integer minOrderQuantity = 1;

    @Column(name = "order_multiple", nullable = false)
    private Integer orderMultiple = 1;

    @Column(name = "lead_time_days", nullable = false)
    private Integer leadTimeDays = 0;

    @Column(nullable = false)
    private Boolean preferred = true;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SupplierMaterial() {
    }

    public static SupplierMaterial create(
            Supplier supplier,
            Product product,
            BigDecimal defaultUnitPrice,
            Integer minOrderQuantity,
            Integer orderMultiple,
            Integer leadTimeDays,
            Boolean preferred,
            String remark
    ) {
        SupplierMaterial material = new SupplierMaterial();
        material.supplier = supplier;
        material.product = product;
        material.update(defaultUnitPrice, minOrderQuantity, orderMultiple, leadTimeDays, preferred, remark);
        return material;
    }

    public void update(
            BigDecimal defaultUnitPrice,
            Integer minOrderQuantity,
            Integer orderMultiple,
            Integer leadTimeDays,
            Boolean preferred,
            String remark
    ) {
        this.defaultUnitPrice = defaultUnitPrice == null ? BigDecimal.ZERO : defaultUnitPrice;
        this.minOrderQuantity = minOrderQuantity == null ? 1 : minOrderQuantity;
        this.orderMultiple = orderMultiple == null ? 1 : orderMultiple;
        this.leadTimeDays = leadTimeDays == null ? 0 : leadTimeDays;
        this.preferred = preferred == null || preferred;
        this.remark = remark;
    }

    public Long getId() {
        return id;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public Product getProduct() {
        return product;
    }

    public BigDecimal getDefaultUnitPrice() {
        return defaultUnitPrice;
    }

    public Integer getMinOrderQuantity() {
        return minOrderQuantity;
    }

    public Integer getOrderMultiple() {
        return orderMultiple;
    }

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public Boolean getPreferred() {
        return preferred;
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
