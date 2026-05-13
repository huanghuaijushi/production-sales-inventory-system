package com.hhjs.psi.sales.goods.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "sales_sku")
public class SalesSku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_goods_id")
    private SalesGoods salesGoods;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "spec_name", length = 64)
    private String specName;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "per_sku_price", precision = 10, scale = 2)
    private BigDecimal perSkuPrice;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "salesSku", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SalesSkuComponent> components = new LinkedHashSet<>();

    protected SalesSku() {
    }

    public static SalesSku create(String code, String name, String specName, String unit, BigDecimal perSkuPrice, String remark) {
        SalesSku sku = new SalesSku();
        sku.code = code;
        sku.name = name;
        sku.specName = specName;
        sku.unit = unit;
        sku.perSkuPrice = perSkuPrice;
        sku.remark = remark;
        sku.enabled = true;
        return sku;
    }

    public void updateBasicInfo(String code, String name, String specName, String unit, BigDecimal perSkuPrice, Boolean enabled, String remark) {
        this.code = code;
        this.name = name;
        this.specName = specName;
        this.unit = unit;
        this.perSkuPrice = perSkuPrice;
        this.enabled = enabled == null ? true : enabled;
        this.remark = remark;
    }

    public void attachToGoods(SalesGoods goods) {
        this.salesGoods = goods;
    }

    public void replaceComponents(List<SalesSkuComponent> newComponents) {
        components.clear();
        for (SalesSkuComponent component : newComponents) {
            component.attachTo(this);
            components.add(component);
        }
    }

    public Long getId() { return id; }
    public SalesGoods getSalesGoods() { return salesGoods; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getSpecName() { return specName; }
    public String getUnit() { return unit; }
    public BigDecimal getPerSkuPrice() { return perSkuPrice; }
    public Boolean getEnabled() { return enabled; }
    public String getRemark() { return remark; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Set<SalesSkuComponent> getComponents() { return components; }
}
