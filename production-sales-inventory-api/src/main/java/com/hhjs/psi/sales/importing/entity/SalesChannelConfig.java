package com.hhjs.psi.sales.importing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "sales_channel_config")
public class SalesChannelConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private ImportSourceType sourceType;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "config_json", columnDefinition = "json")
    private String configJson;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SalesChannelConfig() {
    }

    public static SalesChannelConfig create(String code, String name, ImportSourceType sourceType, Integer sortOrder, String configJson, String remark) {
        SalesChannelConfig config = new SalesChannelConfig();
        config.code = code;
        config.name = name;
        config.sourceType = sourceType;
        config.sortOrder = sortOrder == null ? 0 : sortOrder;
        config.configJson = configJson;
        config.remark = remark;
        config.enabled = true;
        return config;
    }

    public void update(String name, ImportSourceType sourceType, Boolean enabled, Integer sortOrder, String configJson, String remark) {
        this.name = name;
        this.sourceType = sourceType;
        this.enabled = enabled;
        this.sortOrder = sortOrder == null ? 0 : sortOrder;
        this.configJson = configJson;
        this.remark = remark;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public ImportSourceType getSourceType() {
        return sourceType;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public String getConfigJson() {
        return configJson;
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
}
