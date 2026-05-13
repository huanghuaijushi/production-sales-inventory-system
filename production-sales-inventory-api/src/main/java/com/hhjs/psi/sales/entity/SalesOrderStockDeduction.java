package com.hhjs.psi.sales.entity;

import com.hhjs.psi.inventory.entity.Product;
import com.hhjs.psi.inventory.entity.StockBatch;
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

import java.time.Instant;

@Entity
@Table(name = "sales_order_stock_deduction")
public class SalesOrderStockDeduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private SalesOrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private StockBatch batch;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SalesOrderStockDeduction() {
    }

    public static SalesOrderStockDeduction create(SalesOrderItem orderItem, Product product, Integer quantity, StockBatch batch) {
        SalesOrderStockDeduction deduction = new SalesOrderStockDeduction();
        deduction.orderItem = orderItem;
        deduction.product = product;
        deduction.quantity = quantity;
        deduction.batch = batch;
        return deduction;
    }

    public Long getId() { return id; }
    public SalesOrderItem getOrderItem() { return orderItem; }
    public Product getProduct() { return product; }
    public Integer getQuantity() { return quantity; }
    public StockBatch getBatch() { return batch; }
    public Instant getCreatedAt() { return createdAt; }
}
