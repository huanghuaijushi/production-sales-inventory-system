package com.hhjs.psi.sales.goods.entity;

import com.hhjs.psi.sales.importing.entity.SalesChannelConfig;
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
@Table(name = "sales_goods_channel_price")
public class SalesGoodsChannelPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_goods_id", nullable = false)
    private SalesGoods salesGoods;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private SalesChannelConfig channel;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

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

    protected SalesGoodsChannelPrice() {
    }

    public static SalesGoodsChannelPrice create(SalesChannelConfig channel, BigDecimal price, Boolean enabled, String remark) {
        SalesGoodsChannelPrice channelPrice = new SalesGoodsChannelPrice();
        channelPrice.channel = channel;
        channelPrice.price = price;
        channelPrice.enabled = enabled == null ? true : enabled;
        channelPrice.remark = remark;
        return channelPrice;
    }

    public void attachTo(SalesGoods salesGoods) {
        this.salesGoods = salesGoods;
    }

    public Long getId() { return id; }
    public SalesGoods getSalesGoods() { return salesGoods; }
    public SalesChannelConfig getChannel() { return channel; }
    public BigDecimal getPrice() { return price; }
    public Boolean getEnabled() { return enabled; }
    public String getRemark() { return remark; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
