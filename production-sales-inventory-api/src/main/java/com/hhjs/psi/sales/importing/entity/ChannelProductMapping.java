package com.hhjs.psi.sales.importing.entity;

import com.hhjs.psi.sales.goods.entity.SalesSku;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "sales_goods_match_rule")
public class ChannelProductMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private SalesChannelConfig channel;

    @Column(name = "external_product_name", nullable = false, length = 255)
    private String externalProductName;

    @Column(name = "external_spec_name", length = 255)
    private String externalSpecName;

    @Column(name = "external_sku_code", length = 120)
    private String externalSkuCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_sku_id", nullable = false)
    private SalesSku salesSku;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false, length = 30)
    private ChannelProductMatchType matchType;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Integer priority = 100;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ChannelProductMapping() {
    }

    public static ChannelProductMapping create(
            SalesChannelConfig channel,
            String externalProductName,
            String externalSpecName,
            String externalSkuCode,
            SalesSku salesSku,
            ChannelProductMatchType matchType,
            Integer priority,
            String remark
    ) {
        ChannelProductMapping mapping = new ChannelProductMapping();
        mapping.channel = channel;
        mapping.externalProductName = externalProductName;
        mapping.externalSpecName = externalSpecName;
        mapping.externalSkuCode = externalSkuCode;
        mapping.salesSku = salesSku;
        mapping.matchType = matchType;
        mapping.priority = priority == null ? 100 : priority;
        mapping.remark = remark;
        mapping.enabled = true;
        return mapping;
    }

    public void update(
            String externalProductName,
            String externalSpecName,
            String externalSkuCode,
            SalesSku salesSku,
            ChannelProductMatchType matchType,
            Boolean enabled,
            Integer priority,
            String remark
    ) {
        this.externalProductName = externalProductName;
        this.externalSpecName = externalSpecName;
        this.externalSkuCode = externalSkuCode;
        this.salesSku = salesSku;
        this.matchType = matchType;
        this.enabled = enabled;
        this.priority = priority == null ? 100 : priority;
        this.remark = remark;
    }

    public boolean matches(String productName, String specName, String skuCode) {
        String normalizedProductName = normalize(productName);
        String normalizedSpecName = normalize(specName);
        String normalizedSkuCode = normalize(skuCode);
        String expectedName = normalize(externalProductName);
        String expectedSpec = normalize(externalSpecName);
        String expectedSku = normalize(externalSkuCode);
        if (matchType == ChannelProductMatchType.EXACT) {
            return expectedName.equals(normalizedProductName)
                    && equalsNullable(expectedSpec, normalizedSpecName)
                    && equalsNullable(expectedSku, normalizedSkuCode);
        }
        return normalizedProductName.contains(expectedName)
                && containsNullable(normalizedSpecName, expectedSpec)
                && containsNullable(normalizedSkuCode, expectedSku);
    }

    private boolean equalsNullable(String left, String right) {
        return left == null || left.isBlank() ? true : left.equals(right);
    }

    private boolean containsNullable(String source, String target) {
        return target == null || target.isBlank() ? true : source.contains(target);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    public Long getId() {
        return id;
    }

    public SalesChannelConfig getChannel() {
        return channel;
    }

    public String getExternalProductName() {
        return externalProductName;
    }

    public String getExternalSpecName() {
        return externalSpecName;
    }

    public String getExternalSkuCode() {
        return externalSkuCode;
    }

    public SalesSku getSalesSku() {
        return salesSku;
    }

    public ChannelProductMatchType getMatchType() {
        return matchType;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Integer getPriority() {
        return priority;
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
