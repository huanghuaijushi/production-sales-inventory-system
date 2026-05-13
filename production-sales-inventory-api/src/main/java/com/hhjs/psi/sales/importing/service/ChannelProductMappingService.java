package com.hhjs.psi.sales.importing.service;

import com.hhjs.psi.common.exception.BusinessException;
import com.hhjs.psi.sales.goods.entity.SalesSku;
import com.hhjs.psi.sales.goods.repository.SalesSkuRepository;
import com.hhjs.psi.sales.importing.dto.ChannelProductMappingRequest;
import com.hhjs.psi.sales.importing.dto.ChannelProductMappingResponse;
import com.hhjs.psi.sales.importing.entity.ChannelProductMapping;
import com.hhjs.psi.sales.importing.entity.ChannelProductMatchType;
import com.hhjs.psi.sales.importing.entity.SalesChannelConfig;
import com.hhjs.psi.sales.importing.repository.ChannelProductMappingRepository;
import com.hhjs.psi.sales.importing.repository.SalesChannelConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChannelProductMappingService {

    private final ChannelProductMappingRepository mappingRepository;
    private final SalesChannelConfigRepository channelRepository;
    private final SalesSkuRepository salesSkuRepository;

    public ChannelProductMappingService(ChannelProductMappingRepository mappingRepository, SalesChannelConfigRepository channelRepository, SalesSkuRepository salesSkuRepository) {
        this.mappingRepository = mappingRepository;
        this.channelRepository = channelRepository;
        this.salesSkuRepository = salesSkuRepository;
    }

    @Transactional(readOnly = true)
    public List<ChannelProductMappingResponse> getMappings(Long channelId) {
        List<ChannelProductMapping> mappings = channelId == null
                ? mappingRepository.findAll()
                : mappingRepository.findByChannelIdAndEnabledTrueOrderByPriorityAscIdAsc(channelId);
        return mappings.stream().map(ChannelProductMappingResponse::from).toList();
    }

    @Transactional
    public ChannelProductMappingResponse create(ChannelProductMappingRequest request) {
        SalesChannelConfig channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> BusinessException.badRequest("销售渠道不存在: " + request.channelId()));
        SalesSku salesSku = salesSkuRepository.findById(request.salesSkuId())
                .orElseThrow(() -> BusinessException.badRequest("销售SKU不存在: " + request.salesSkuId()));
        ChannelProductMapping mapping = ChannelProductMapping.create(
                channel,
                normalizeRequired(request.externalProductName(), "外部商品名称不能为空"),
                normalizeOptional(request.externalSpecName()),
                normalizeOptional(request.externalSkuCode()),
                salesSku,
                parseMatchType(request.matchType()),
                request.priority(),
                normalizeOptional(request.remark())
        );
        return ChannelProductMappingResponse.from(mappingRepository.save(mapping));
    }

    @Transactional
    public ChannelProductMappingResponse update(Long id, ChannelProductMappingRequest request) {
        ChannelProductMapping mapping = mappingRepository.findById(id)
                .orElseThrow(() -> BusinessException.badRequest("商品映射规则不存在: " + id));
        SalesChannelConfig channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> BusinessException.badRequest("销售渠道不存在: " + request.channelId()));
        SalesSku salesSku = salesSkuRepository.findById(request.salesSkuId())
                .orElseThrow(() -> BusinessException.badRequest("销售SKU不存在: " + request.salesSkuId()));
        mapping.update(
                normalizeRequired(request.externalProductName(), "外部商品名称不能为空"),
                normalizeOptional(request.externalSpecName()),
                normalizeOptional(request.externalSkuCode()),
                salesSku,
                parseMatchType(request.matchType()),
                request.enabled(),
                request.priority(),
                normalizeOptional(request.remark())
        );
        return ChannelProductMappingResponse.from(mappingRepository.save(mapping));
    }

    private ChannelProductMatchType parseMatchType(String matchType) {
        if (matchType == null || matchType.isBlank()) {
            throw BusinessException.badRequest("匹配类型不能为空");
        }
        try {
            return ChannelProductMatchType.valueOf(matchType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw BusinessException.badRequest("无效的匹配类型: " + matchType + "，有效值为: EXACT, CONTAINS");
        }
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw BusinessException.badRequest(message);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
