package com.hhjs.psi.sales.goods.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "sales_goods")
public class SalesGoods {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 64)
    private String category;

    @Column(length = 64)
    private String specification;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "default_price", precision = 10, scale = 2)
    private BigDecimal defaultPrice;

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

    @OneToMany(mappedBy = "salesGoods", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SalesGoodsComponent> components = new LinkedHashSet<>();

    @OneToMany(mappedBy = "salesGoods", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SalesGoodsChannelPrice> channelPrices = new LinkedHashSet<>();

    protected SalesGoods() {
    }

    public static SalesGoods create(String code, String name, String category, String specification, String unit, BigDecimal defaultPrice, String remark) {
        SalesGoods goods = new SalesGoods();
        goods.code = code;
        goods.name = name;
        goods.category = category;
        goods.specification = specification;
        goods.unit = unit;
        goods.defaultPrice = defaultPrice;
        goods.remark = remark;
        goods.enabled = true;
        return goods;
    }

    public void updateBasicInfo(String code, String name, String category, String specification, String unit, BigDecimal defaultPrice, Boolean enabled, String remark) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.specification = specification;
        this.unit = unit;
        this.defaultPrice = defaultPrice;
        this.enabled = enabled == null ? true : enabled;
        this.remark = remark;
    }

    public void replaceComponents(List<SalesGoodsComponent> newComponents) {
        components.clear();
        for (SalesGoodsComponent component : newComponents) {
            component.attachTo(this);
            components.add(component);
        }
    }

    public void replaceChannelPrices(List<SalesGoodsChannelPrice> newPrices) {
        channelPrices.clear();
        for (SalesGoodsChannelPrice price : newPrices) {
            price.attachTo(this);
            channelPrices.add(price);
        }
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getSpecification() { return specification; }
    public String getUnit() { return unit; }
    public BigDecimal getDefaultPrice() { return defaultPrice; }
    public Boolean getEnabled() { return enabled; }
    public String getRemark() { return remark; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Set<SalesGoodsComponent> getComponents() { return components; }
    public Set<SalesGoodsChannelPrice> getChannelPrices() { return channelPrices; }
}
