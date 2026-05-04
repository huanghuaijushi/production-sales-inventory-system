package com.hhjs.psi.purchase.entity;

import com.hhjs.psi.supplier.entity.Supplier;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "purchase_order")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, length = 64, unique = true)
    private String orderNo;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PurchaseOrderStatus status;

    @Column(name = "expected_arrival_date")
    private LocalDate expectedArrivalDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "operator_name", nullable = false, length = 80)
    private String operatorName;

    @Column(length = 500)
    private String remark;

    @Column(name = "inbound_at")
    private Instant inboundAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    protected PurchaseOrder() {
    }

    public static PurchaseOrder create(
            String orderNo,
            Supplier supplier,
            PurchaseOrderStatus status,
            LocalDate expectedArrivalDate,
            Long operatorId,
            String operatorName,
            String remark
    ) {
        PurchaseOrder order = new PurchaseOrder();
        order.orderNo = orderNo;
        order.supplier = supplier;
        order.status = status;
        order.expectedArrivalDate = expectedArrivalDate;
        order.operatorId = operatorId;
        order.operatorName = operatorName;
        order.remark = remark;
        return order;
    }

    public void updateBasicInfo(
            Supplier supplier,
            PurchaseOrderStatus status,
            LocalDate expectedArrivalDate,
            String remark
    ) {
        this.supplier = supplier;
        this.status = status;
        this.expectedArrivalDate = expectedArrivalDate;
        this.remark = remark;
    }

    public void replaceItems(List<PurchaseOrderItem> newItems) {
        items.clear();
        newItems.forEach(item -> item.attachTo(this));
        items.addAll(newItems);
        recalculateTotalAmount();
    }

    public void recalculateTotalAmount() {
        this.totalAmount = items.stream()
                .map(PurchaseOrderItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void markInbound(Instant inboundAt) {
        this.status = PurchaseOrderStatus.INBOUNDED;
        this.inboundAt = inboundAt;
    }

    public void cancel() {
        this.status = PurchaseOrderStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public PurchaseOrderStatus getStatus() {
        return status;
    }

    public LocalDate getExpectedArrivalDate() {
        return expectedArrivalDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
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

    public Instant getInboundAt() {
        return inboundAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<PurchaseOrderItem> getItems() {
        return items;
    }
}
