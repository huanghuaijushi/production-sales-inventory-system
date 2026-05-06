package com.hhjs.psi.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "stock")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "locked_quantity", nullable = false)
    private Integer lockedQuantity = 0;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 0;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Stock() {
    }

    public static Stock create(Product product) {
        Stock stock = new Stock();
        stock.product = product;
        stock.quantity = 0;
        stock.lockedQuantity = 0;
        stock.availableQuantity = 0;
        return stock;
    }

    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
        this.availableQuantity = quantity - lockedQuantity;
    }

    public void lockQuantity(Integer amount) {
        this.lockedQuantity += amount;
        this.availableQuantity = quantity - lockedQuantity;
    }

    public void unlockQuantity(Integer amount) {
        this.lockedQuantity -= amount;
        this.availableQuantity = quantity - lockedQuantity;
    }

    public void shipLockedQuantity(Integer amount) {
        this.quantity -= amount;
        this.lockedQuantity -= amount;
        this.availableQuantity = quantity - lockedQuantity;
    }

    public boolean isLowStock() {
        return quantity <= product.getAlertQuantity();
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getLockedQuantity() {
        return lockedQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
