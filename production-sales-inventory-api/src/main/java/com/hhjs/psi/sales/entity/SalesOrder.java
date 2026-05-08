package com.hhjs.psi.sales.entity;

import com.hhjs.psi.auth.entity.SysUser;
import com.hhjs.psi.sales.importing.entity.ImportSourceType;
import com.hhjs.psi.sales.importing.entity.OrderImportBatch;
import com.hhjs.psi.sales.importing.entity.SalesChannelConfig;
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
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "sales_order")
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 64)
    private String orderNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SalesChannel channel;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "channel_id")
    private SalesChannelConfig channelConfig;

    @Column(name = "external_order_no", length = 120)
    private String externalOrderNo;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "import_batch_id")
    private OrderImportBatch importBatch;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 30)
    private ImportSourceType sourceType;

    @Column(name = "source_remark", length = 500)
    private String sourceRemark;

    @Column(name = "customer_name", length = 120)
    private String customerName;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "customer_address", length = 255)
    private String customerAddress;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SalesOrderStatus status;

    @Column(name = "order_date", nullable = false)
    private Instant orderDate;

    @Column(name = "ship_date")
    private Instant shipDate;

    @Column(name = "complete_date")
    private Instant completeDate;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private SysUser operator;

    @Column(name = "operator_name", nullable = false, length = 80)
    private String operatorName;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesOrderItem> items = new ArrayList<>();

    protected SalesOrder() {
    }

    public static SalesOrder create(
            String orderNo,
            SalesChannel channel,
            SalesChannelConfig channelConfig,
            String externalOrderNo,
            OrderImportBatch importBatch,
            ImportSourceType sourceType,
            String sourceRemark,
            String customerName,
            String customerPhone,
            String customerAddress,
            SysUser operator,
            String operatorName,
            String remark
    ) {
        SalesOrder order = new SalesOrder();
        order.orderNo = orderNo;
        order.channel = channel;
        order.channelConfig = channelConfig;
        order.externalOrderNo = externalOrderNo;
        order.importBatch = importBatch;
        order.sourceType = sourceType;
        order.sourceRemark = sourceRemark;
        order.customerName = customerName;
        order.customerPhone = customerPhone;
        order.customerAddress = customerAddress;
        order.operator = operator;
        order.operatorName = operatorName;
        order.remark = remark;
        order.status = SalesOrderStatus.PENDING;
        order.orderDate = Instant.now();
        return order;
    }

    public void replaceItems(List<SalesOrderItem> newItems) {
        items.clear();
        totalAmount = BigDecimal.ZERO;
        for (SalesOrderItem item : newItems) {
            item.attachTo(this);
            items.add(item);
            totalAmount = totalAmount.add(item.getSubtotal());
        }
    }

    public void updateBasicInfo(SalesChannel channel, SalesChannelConfig channelConfig, String customerName, String customerPhone, String customerAddress, String remark) {
        this.channel = channel;
        this.channelConfig = channelConfig;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerAddress = customerAddress;
        this.remark = remark;
    }

    public void markShipped(Instant shipDate) {
        this.status = SalesOrderStatus.SHIPPED;
        this.shipDate = shipDate;
    }

    public void markCompleted(Instant completeDate) {
        this.status = SalesOrderStatus.COMPLETED;
        this.completeDate = completeDate;
    }

    public void cancel() {
        this.status = SalesOrderStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public SalesChannel getChannel() {
        return channel;
    }

    public SalesChannelConfig getChannelConfig() {
        return channelConfig;
    }

    public String getExternalOrderNo() {
        return externalOrderNo;
    }

    public OrderImportBatch getImportBatch() {
        return importBatch;
    }

    public ImportSourceType getSourceType() {
        return sourceType;
    }

    public String getSourceRemark() {
        return sourceRemark;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public SalesOrderStatus getStatus() {
        return status;
    }

    public Instant getOrderDate() {
        return orderDate;
    }

    public Instant getShipDate() {
        return shipDate;
    }

    public Instant getCompleteDate() {
        return completeDate;
    }

    public SysUser getOperator() {
        return operator;
    }

    public String getOperatorName() {
        return operatorName;
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

    public List<SalesOrderItem> getItems() {
        return items;
    }
}
