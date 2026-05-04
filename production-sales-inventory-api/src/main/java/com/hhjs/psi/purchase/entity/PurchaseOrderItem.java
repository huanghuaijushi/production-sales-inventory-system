package com.hhjs.psi.purchase.entity;

import com.hhjs.psi.inventory.entity.Product;
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
@Table(name = "purchase_order_item")
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder order;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_code", nullable = false, length = 64)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 120)
    private String productName;

    @Column(name = "product_specification", length = 64)
    private String productSpecification;

    @Column(name = "product_unit", nullable = false, length = 20)
    private String productUnit;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PurchaseOrderItem() {
    }

    public static PurchaseOrderItem create(Product product, Integer quantity, BigDecimal unitPrice) {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.product = product;
        item.productCode = product.getCode();
        item.productName = product.getName();
        item.productSpecification = product.getSpecification();
        item.productUnit = product.getUnit();
        item.quantity = quantity;
        item.unitPrice = unitPrice;
        item.amount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return item;
    }

    public void attachTo(PurchaseOrder order) {
        this.order = order;
    }

    public Long getId() {
        return id;
    }

    public PurchaseOrder getOrder() {
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

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
