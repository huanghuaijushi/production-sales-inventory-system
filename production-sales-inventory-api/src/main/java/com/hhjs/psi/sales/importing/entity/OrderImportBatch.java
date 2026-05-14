package com.hhjs.psi.sales.importing.entity;

import com.hhjs.psi.auth.entity.SysUser;
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

import java.time.Instant;

@Entity
@Table(name = "order_import_batch")
public class OrderImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "import_no", nullable = false, unique = true, length = 64)
    private String importNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private SalesChannelConfig channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private ImportSourceType sourceType;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "raw_text", columnDefinition = "mediumtext")
    private String rawText;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount = 0;

    @Column(name = "parsed_count", nullable = false)
    private Integer parsedCount = 0;

    @Column(name = "ready_count", nullable = false)
    private Integer readyCount = 0;

    @Column(name = "converted_count", nullable = false)
    private Integer convertedCount = 0;

    @Column(name = "error_count", nullable = false)
    private Integer errorCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ImportBatchStatus status = ImportBatchStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private SysUser operator;

    @Column(name = "operator_name", nullable = false, length = 80)
    private String operatorName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OrderImportBatch() {
    }

    public static OrderImportBatch create(String importNo, SalesChannelConfig channel, ImportSourceType sourceType, String fileName, String rawText, SysUser operator, String operatorName) {
        OrderImportBatch batch = new OrderImportBatch();
        batch.importNo = importNo;
        batch.channel = channel;
        batch.sourceType = sourceType;
        batch.fileName = fileName;
        batch.rawText = rawText;
        batch.operator = operator;
        batch.operatorName = operatorName;
        batch.status = ImportBatchStatus.DRAFT;
        return batch;
    }

    public void updateStats(int totalCount, int parsedCount, int readyCount, int convertedCount, int errorCount) {
        this.totalCount = totalCount;
        this.parsedCount = parsedCount;
        this.readyCount = readyCount;
        this.convertedCount = convertedCount;
        this.errorCount = errorCount;
        if (status != ImportBatchStatus.CONFIRMED && status != ImportBatchStatus.CANCELLED) {
            this.status = ImportBatchStatus.PARSED;
        }
    }

    public void markConfirmed(int convertedCount) {
        this.convertedCount = convertedCount;
        this.status = ImportBatchStatus.CONFIRMED;
    }

    public Long getId() { return id; }
    public String getImportNo() { return importNo; }
    public SalesChannelConfig getChannel() { return channel; }
    public ImportSourceType getSourceType() { return sourceType; }
    public String getFileName() { return fileName; }
    public String getRawText() { return rawText; }
    public Integer getTotalCount() { return totalCount; }
    public Integer getParsedCount() { return parsedCount; }
    public Integer getReadyCount() { return readyCount; }
    public Integer getConvertedCount() { return convertedCount; }
    public Integer getErrorCount() { return errorCount; }
    public ImportBatchStatus getStatus() { return status; }
    public SysUser getOperator() { return operator; }
    public String getOperatorName() { return operatorName; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
