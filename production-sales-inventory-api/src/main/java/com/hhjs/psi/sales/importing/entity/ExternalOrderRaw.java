package com.hhjs.psi.sales.importing.entity;

import com.hhjs.psi.sales.entity.SalesOrder;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "external_order_raw")
public class ExternalOrderRaw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private OrderImportBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private SalesChannelConfig channel;

    @Column(name = "external_order_no", nullable = false, length = 120)
    private String externalOrderNo;

    @Column(name = "raw_payload", columnDefinition = "json")
    private String rawPayload;

    @Column(name = "order_time")
    private Instant orderTime;

    @Column(name = "customer_name", length = 120)
    private String customerName;

    @Column(name = "customer_phone", length = 30)
    private String customerPhone;

    @Column(name = "customer_address", length = 500)
    private String customerAddress;

    @Column(length = 60)
    private String province;

    @Column(length = 60)
    private String city;

    @Column(length = 60)
    private String district;

    @Column(name = "logistics_company", length = 80)
    private String logisticsCompany;

    @Column(name = "tracking_no", length = 120)
    private String trackingNo;

    @Column(name = "buyer_message", length = 500)
    private String buyerMessage;

    @Column(name = "seller_remark", length = 500)
    private String sellerRemark;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExternalOrderStatus status = ExternalOrderStatus.WAIT_MATCH;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;

    @OneToMany(mappedBy = "externalOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExternalOrderItemRaw> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ExternalOrderRaw() {
    }

    public static ExternalOrderRaw create(OrderImportBatch batch, SalesChannelConfig channel, String externalOrderNo, String rawPayload, String customerName, String customerPhone, String customerAddress, String buyerMessage, String sellerRemark) {
        ExternalOrderRaw order = new ExternalOrderRaw();
        order.batch = batch;
        order.channel = channel;
        order.externalOrderNo = externalOrderNo;
        order.rawPayload = rawPayload;
        order.customerName = customerName;
        order.customerPhone = customerPhone;
        order.customerAddress = customerAddress;
        order.buyerMessage = buyerMessage;
        order.sellerRemark = sellerRemark;
        return order;
    }

    public void replaceItems(List<ExternalOrderItemRaw> newItems) {
        items.clear();
        for (ExternalOrderItemRaw item : newItems) {
            item.attachTo(this);
            items.add(item);
        }
    }

    public void refreshStatus() {
        if (items.isEmpty()) {
            markError("订单明细不能为空");
            return;
        }
        boolean allMatched = items.stream().allMatch(item -> item.getMatchStatus() == ExternalOrderItemMatchStatus.MATCHED);
        if (allMatched) {
            status = ExternalOrderStatus.READY;
            errorMessage = null;
        } else {
            status = ExternalOrderStatus.WAIT_MATCH;
            errorMessage = "存在未匹配商品";
        }
    }

    public void markError(String message) {
        status = ExternalOrderStatus.ERROR;
        errorMessage = message;
    }

    public void markConverted(SalesOrder salesOrder) {
        this.salesOrder = salesOrder;
        this.status = ExternalOrderStatus.CONVERTED;
        this.errorMessage = null;
    }

    public void updateReceiverInfo(String customerName, String customerPhone, String customerAddress, String buyerMessage, String sellerRemark) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerAddress = customerAddress;
        this.buyerMessage = buyerMessage;
        this.sellerRemark = sellerRemark;
    }

    public Long getId() { return id; }
    public OrderImportBatch getBatch() { return batch; }
    public SalesChannelConfig getChannel() { return channel; }
    public String getExternalOrderNo() { return externalOrderNo; }
    public String getRawPayload() { return rawPayload; }
    public Instant getOrderTime() { return orderTime; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getCustomerAddress() { return customerAddress; }
    public String getProvince() { return province; }
    public String getCity() { return city; }
    public String getDistrict() { return district; }
    public String getLogisticsCompany() { return logisticsCompany; }
    public String getTrackingNo() { return trackingNo; }
    public String getBuyerMessage() { return buyerMessage; }
    public String getSellerRemark() { return sellerRemark; }
    public ExternalOrderStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public SalesOrder getSalesOrder() { return salesOrder; }
    public List<ExternalOrderItemRaw> getItems() { return items; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
