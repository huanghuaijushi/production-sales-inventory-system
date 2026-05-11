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
@Table(name = "sales_goods_component")
public class SalesGoodsComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_goods_id", nullable = false)
    private SalesGoods salesGoods;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity_per_unit", nullable = false, precision = 12, scale = 4)
    private BigDecimal quantityPerUnit;

    @Column(name = "loss_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal lossRate = BigDecimal.ZERO;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SalesGoodsComponent() {
    }

    public static SalesGoodsComponent create(Product product, BigDecimal quantityPerUnit, BigDecimal lossRate, String remark) {
        SalesGoodsComponent component = new SalesGoodsComponent();
        component.product = product;
        component.quantityPerUnit = quantityPerUnit;
        component.lossRate = lossRate == null ? BigDecimal.ZERO : lossRate;
        component.remark = remark;
        return component;
    }

    public void attachTo(SalesGoods salesGoods) {
        this.salesGoods = salesGoods;
    }

    public Long getId() { return id; }
    public SalesGoods getSalesGoods() { return salesGoods; }
    public Product getProduct() { return product; }
    public BigDecimal getQuantityPerUnit() { return quantityPerUnit; }
    public BigDecimal getLossRate() { return lossRate; }
    public String getRemark() { return remark; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
