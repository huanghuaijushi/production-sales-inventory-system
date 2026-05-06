package com.hhjs.psi.sales.importing.service;

import com.hhjs.psi.common.exception.BusinessException;
import com.hhjs.psi.sales.importing.dto.SalesChannelConfigRequest;
import com.hhjs.psi.sales.importing.dto.SalesChannelConfigResponse;
import com.hhjs.psi.sales.importing.entity.ImportSourceType;
import com.hhjs.psi.sales.importing.entity.SalesChannelConfig;
import com.hhjs.psi.sales.importing.repository.SalesChannelConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalesChannelConfigService {

    private final SalesChannelConfigRepository repository;

    public SalesChannelConfigService(SalesChannelConfigRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SalesChannelConfigResponse> getChannels(boolean enabledOnly) {
        List<SalesChannelConfig> channels = enabledOnly
                ? repository.findByEnabledTrueOrderBySortOrderAscIdAsc()
                : repository.findAllByOrderBySortOrderAscIdAsc();
        return channels.stream().map(SalesChannelConfigResponse::from).toList();
    }

    @Transactional
    public SalesChannelConfigResponse create(SalesChannelConfigRequest request) {
        String code = normalizeRequired(request.code()).toUpperCase();
        if (repository.existsByCode(code)) {
            throw BusinessException.badRequest("渠道编码已存在: " + code);
        }
        SalesChannelConfig channel = SalesChannelConfig.create(
                code,
                normalizeRequired(request.name()),
                parseSourceType(request.sourceType()),
                request.sortOrder(),
                normalizeOptional(request.configJson()),
                normalizeOptional(request.remark())
        );
        channel.update(channel.getName(), channel.getSourceType(), request.enabled(), channel.getSortOrder(), channel.getConfigJson(), channel.getRemark());
        return SalesChannelConfigResponse.from(repository.save(channel));
    }

    @Transactional
    public SalesChannelConfigResponse update(Long id, SalesChannelConfigRequest request) {
        SalesChannelConfig channel = repository.findById(id)
                .orElseThrow(() -> BusinessException.badRequest("销售渠道不存在: " + id));
        String code = normalizeRequired(request.code()).toUpperCase();
        if (!channel.getCode().equals(code) && repository.existsByCodeAndIdNot(code, id)) {
            throw BusinessException.badRequest("渠道编码已存在: " + code);
        }
        channel.update(
                normalizeRequired(request.name()),
                parseSourceType(request.sourceType()),
                request.enabled(),
                request.sortOrder(),
                normalizeOptional(request.configJson()),
                normalizeOptional(request.remark())
        );
        return SalesChannelConfigResponse.from(repository.save(channel));
    }

    private ImportSourceType parseSourceType(String value) {
        try {
            return ImportSourceType.valueOf(normalizeRequired(value).toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest("不支持的来源类型: " + value);
        }
    }

    private String normalizeRequired(String value) {
        if (value == null || value.isBlank()) {
            throw BusinessException.badRequest("必填字段不能为空");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
