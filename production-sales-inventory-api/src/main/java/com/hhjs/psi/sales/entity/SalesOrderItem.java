package com.hhjs.psi.sales.entity;

import com.hhjs.psi.inventory.entity.Product;
import com.hhjs.psi.sales.importing.entity.ChannelProductMapping;
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
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_code", length = 64)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 120)
    private String productName;

    @Column(name = "product_specification", length = 64)
    private String productSpecification;

    @Column(name = "product_unit", length = 20)
    private String productUnit;

    @Column(name = "product_category", length = 64)
    private String productCategory;

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

    public static SalesOrderItem create(Product product, Integer quantity, BigDecimal unitPrice) {
        return create(product, quantity, unitPrice, null, null, null, null);
    }

    public static SalesOrderItem create(
            Product product,
            Integer quantity,
            BigDecimal unitPrice,
            String externalProductName,
            String externalSpecName,
            BigDecimal externalQuantity,
            ChannelProductMapping mapping
    ) {
        SalesOrderItem item = new SalesOrderItem();
        item.product = product;
        item.productCode = product.getCode();
        item.productName = product.getName();
        item.productSpecification = product.getSpecification();
        item.productUnit = product.getUnit();
        item.productCategory = product.getCategory();
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

    public Product getProduct() {
        return product;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductSpecification() {
        return productSpecification;
    }

    public String getProductUnit() {
        return productUnit;
    }

    public String getProductCategory() {
        return productCategory;
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
