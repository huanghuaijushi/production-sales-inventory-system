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

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "external_order_item_raw")
public class ExternalOrderItemRaw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_order_id", nullable = false)
    private ExternalOrderRaw externalOrder;

    @Column(name = "external_product_name", nullable = false, length = 255)
    private String externalProductName;

    @Column(name = "external_spec_name", length = 255)
    private String externalSpecName;

    @Column(name = "external_sku_code", length = 120)
    private String externalSkuCode;

    @Column(name = "external_quantity", nullable = false, precision = 12, scale = 4)
    private BigDecimal externalQuantity;

    @Column(name = "external_unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal externalUnitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_sales_sku_id")
    private SalesSku matchedSalesSku;

    @Column(name = "matched_sku_name", length = 120)
    private String matchedSkuName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mapping_id")
    private ChannelProductMapping mapping;

    @Column(name = "sale_quantity")
    private Integer saleQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", nullable = false, length = 30)
    private ExternalOrderItemMatchStatus matchStatus = ExternalOrderItemMatchStatus.UNMATCHED;

    @Column(name = "match_message", length = 500)
    private String matchMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ExternalOrderItemRaw() {
    }

    public static ExternalOrderItemRaw create(String externalProductName, String externalSpecName, String externalSkuCode, BigDecimal externalQuantity, BigDecimal externalUnitPrice) {
        ExternalOrderItemRaw item = new ExternalOrderItemRaw();
        item.externalProductName = externalProductName;
        item.externalSpecName = externalSpecName;
        item.externalSkuCode = externalSkuCode;
        item.externalQuantity = externalQuantity;
        item.externalUnitPrice = externalUnitPrice;
        item.matchStatus = ExternalOrderItemMatchStatus.UNMATCHED;
        return item;
    }

    public void attachTo(ExternalOrderRaw externalOrder) {
        this.externalOrder = externalOrder;
    }

    public void applyMatched(ChannelProductMapping mapping, SalesSku salesSku, Integer saleQuantity, String message) {
        this.mapping = mapping;
        this.matchedSalesSku = salesSku;
        this.matchedSkuName = salesSku.getName();
        this.saleQuantity = saleQuantity;
        this.matchStatus = ExternalOrderItemMatchStatus.MATCHED;
        this.matchMessage = message;
    }

    public void markUnmatched(String message) {
        this.mapping = null;
        this.matchedSalesSku = null;
        this.matchedSkuName = null;
        this.saleQuantity = null;
        this.matchStatus = ExternalOrderItemMatchStatus.UNMATCHED;
        this.matchMessage = message;
    }

    public void updateRaw(String externalProductName, String externalSpecName, String externalSkuCode, BigDecimal externalQuantity, BigDecimal externalUnitPrice) {
        this.externalProductName = externalProductName;
        this.externalSpecName = externalSpecName;
        this.externalSkuCode = externalSkuCode;
        this.externalQuantity = externalQuantity;
        this.externalUnitPrice = externalUnitPrice;
        markUnmatched("已编辑，等待重新匹配");
    }

    public Long getId() { return id; }
    public ExternalOrderRaw getExternalOrder() { return externalOrder; }
    public String getExternalProductName() { return externalProductName; }
    public String getExternalSpecName() { return externalSpecName; }
    public String getExternalSkuCode() { return externalSkuCode; }
    public BigDecimal getExternalQuantity() { return externalQuantity; }
    public BigDecimal getExternalUnitPrice() { return externalUnitPrice; }
    public SalesSku getMatchedSalesSku() { return matchedSalesSku; }
    public String getMatchedSkuName() { return matchedSkuName; }
    public ChannelProductMapping getMapping() { return mapping; }
    public Integer getSaleQuantity() { return saleQuantity; }
    public ExternalOrderItemMatchStatus getMatchStatus() { return matchStatus; }
    public String getMatchMessage() { return matchMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
