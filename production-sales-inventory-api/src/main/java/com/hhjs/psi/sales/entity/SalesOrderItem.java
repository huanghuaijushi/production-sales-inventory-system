package com.hhjs.psi.sales.entity;

import com.hhjs.psi.sales.importing.entity.ChannelProductMapping;
import com.hhjs.psi.sales.goods.entity.SalesGoods;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "sales_order_item")
public class SalesOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private SalesOrder order;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "sales_goods_id", nullable = false)
    private SalesGoods salesGoods;

    @Column(name = "goods_code", length = 64)
    private String goodsCode;

    @Column(name = "goods_name", nullable = false, length = 120)
    private String goodsName;

    @Column(name = "goods_specification", length = 64)
    private String goodsSpecification;

    @Column(name = "goods_unit", length = 20)
    private String goodsUnit;

    @Column(name = "goods_category", length = 64)
    private String goodsCategory;

    @Column(name = "external_product_name", length = 255)
    private String externalProductName;

    @Column(name = "external_spec_name", length = 255)
    private String externalSpecName;

    @Column(name = "external_quantity", precision = 12, scale = 4)
    private BigDecimal externalQuantity;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "mapping_id")
    private ChannelProductMapping mapping;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SalesOrderItem() {
    }

    public static SalesOrderItem create(SalesGoods salesGoods, Integer quantity, BigDecimal unitPrice) {
        return create(salesGoods, quantity, unitPrice, null, null, null, null);
    }

    public static SalesOrderItem create(
            SalesGoods salesGoods,
            Integer quantity,
            BigDecimal unitPrice,
            String externalProductName,
            String externalSpecName,
            BigDecimal externalQuantity,
            ChannelProductMapping mapping
    ) {
        SalesOrderItem item = new SalesOrderItem();
        item.salesGoods = salesGoods;
        item.goodsCode = salesGoods.getCode();
        item.goodsName = salesGoods.getName();
        item.goodsSpecification = salesGoods.getSpecification();
        item.goodsUnit = salesGoods.getUnit();
        item.goodsCategory = salesGoods.getCategory();
        item.externalProductName = externalProductName;
        item.externalSpecName = externalSpecName;
        item.externalQuantity = externalQuantity;
        item.mapping = mapping;
        item.quantity = quantity;
        item.unitPrice = unitPrice;
        item.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return item;
    }

    public void attachTo(SalesOrder order) {
        this.order = order;
    }

    public Long getId() {
        return id;
    }

    public SalesOrder getOrder() {
        return order;
    }

    public SalesGoods getSalesGoods() {
        return salesGoods;
    }

    public String getGoodsCode() {
        return goodsCode;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public String getGoodsSpecification() {
        return goodsSpecification;
    }

    public String getGoodsUnit() {
        return goodsUnit;
    }

    public String getGoodsCategory() {
        return goodsCategory;
    }

    public String getExternalProductName() {
        return externalProductName;
    }

    public String getExternalSpecName() {
        return externalSpecName;
    }

    public BigDecimal getExternalQuantity() {
        return externalQuantity;
    }

    public ChannelProductMapping getMapping() {
        return mapping;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
