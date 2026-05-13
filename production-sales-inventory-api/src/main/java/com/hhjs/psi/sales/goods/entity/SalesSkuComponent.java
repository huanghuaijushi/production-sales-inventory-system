package com.hhjs.psi.sales.goods.entity;

import com.hhjs.psi.inventory.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

@Entity
@Table(name = "sales_sku_component")
public class SalesSkuComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_sku_id", nullable = false)
    private SalesSku salesSku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantity;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SalesSkuComponent() {
    }

    public static SalesSkuComponent create(Product product, BigDecimal quantity, String remark) {
        SalesSkuComponent component = new SalesSkuComponent();
        component.product = product;
        component.quantity = quantity;
        component.remark = remark;
        return component;
    }

    public void attachTo(SalesSku salesSku) {
        this.salesSku = salesSku;
    }

    public Long getId() { return id; }
    public SalesSku getSalesSku() { return salesSku; }
    public Product getProduct() { return product; }
    public BigDecimal getQuantity() { return quantity; }
    public String getRemark() { return remark; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
