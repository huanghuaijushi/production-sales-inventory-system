package com.hhjs.psi.inventory.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_check_order")
public class StockCheckOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "check_no", nullable = false, unique = true, length = 64)
    private String checkNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockCheckOrderStatus status;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "operator_name", nullable = false, length = 80)
    private String operatorName;

    @Column(length = 500)
    private String remark;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockCheckOrderItem> items = new ArrayList<>();

    protected StockCheckOrder() {
    }

    public static StockCheckOrder create(String checkNo, Long operatorId, String operatorName, String remark) {
        StockCheckOrder order = new StockCheckOrder();
        order.checkNo = checkNo;
        order.operatorId = operatorId;
        order.operatorName = operatorName;
        order.remark = remark;
        order.status = StockCheckOrderStatus.DRAFT;
        return order;
    }

    public void addItem(StockCheckOrderItem item) {
        item.attachTo(this);
        items.add(item);
    }

    public void confirm(Instant confirmedAt) {
        this.status = StockCheckOrderStatus.CONFIRMED;
        this.confirmedAt = confirmedAt;
    }

    public void cancel() {
        this.status = StockCheckOrderStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public String getCheckNo() {
        return checkNo;
    }

    public StockCheckOrderStatus getStatus() {
        return status;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public String getRemark() {
        return remark;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<StockCheckOrderItem> getItems() {
        return items;
    }
}
