package com.hhjs.psi.sales.importing.service;

import com.hhjs.psi.common.exception.BusinessException;
import com.hhjs.psi.sales.importing.dto.ChannelProductMappingRequest;
import com.hhjs.psi.sales.importing.dto.ChannelProductMappingResponse;
import com.hhjs.psi.sales.importing.entity.ChannelProductMapping;
import com.hhjs.psi.sales.importing.entity.ChannelProductMatchType;
import com.hhjs.psi.sales.importing.entity.SalesChannelConfig;
import com.hhjs.psi.sales.importing.repository.ChannelProductMappingRepository;
import com.hhjs.psi.sales.importing.repository.SalesChannelConfigRepository;
import com.hhjs.psi.sales.goods.entity.SalesGoods;
import com.hhjs.psi.sales.goods.repository.SalesGoodsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ChannelProductMappingService {

    private final ChannelProductMappingRepository mappingRepository;
    private final SalesChannelConfigRepository channelRepository;
    private final SalesGoodsRepository salesGoodsRepository;

    public ChannelProductMappingService(ChannelProductMappingRepository mappingRepository, SalesChannelConfigRepository channelRepository, SalesGoodsRepository salesGoodsRepository) {
        this.mappingRepository = mappingRepository;
        this.channelRepository = channelRepository;
        this.salesGoodsRepository = salesGoodsRepository;
    }

    @Transactional(readOnly = true)
    public List<ChannelProductMappingResponse> getMappings(Long channelId) {
        if (channelId == null) {
            return mappingRepository.findAll().stream().map(ChannelProductMappingResponse::from).toList();
        }
        return mappingRepository.findByChannelIdOrderByPriorityAscIdAsc(channelId).stream().map(ChannelProductMappingResponse::from).toList();
    }

    @Transactional
    public ChannelProductMappingResponse create(ChannelProductMappingRequest request) {
        SalesChannelConfig channel = findChannel(request.channelId());
        SalesGoods salesGoods = findSalesGoods(request.salesGoodsId());
        ChannelProductMapping mapping = ChannelProductMapping.create(
                channel,
                normalizeRequired(request.externalProductName()),
                normalizeOptional(request.externalSpecName()),
                normalizeOptional(request.externalSkuCode()),
                salesGoods,
                normalizeMultiplier(request.quantityMultiplier()),
                normalizeDefaultUnitPrice(request.defaultUnitPrice()),
                parseMatchType(request.matchType()),
                request.priority(),
                normalizeOptional(request.remark())
        );
        mapping.update(mapping.getExternalProductName(), mapping.getExternalSpecName(), mapping.getExternalSkuCode(), salesGoods, mapping.getQuantityMultiplier(), mapping.getDefaultUnitPrice(), mapping.getMatchType(), request.enabled(), mapping.getPriority(), mapping.getRemark());
        return ChannelProductMappingResponse.from(mappingRepository.save(mapping));
    }

    @Transactional
    public ChannelProductMappingResponse update(Long id, ChannelProductMappingRequest request) {
        ChannelProductMapping mapping = mappingRepository.findById(id)
                .orElseThrow(() -> BusinessException.badRequest("商品映射不存在: " + id));
        SalesGoods salesGoods = findSalesGoods(request.salesGoodsId());
        mapping.update(
                normalizeRequired(request.externalProductName()),
                normalizeOptional(request.externalSpecName()),
                normalizeOptional(request.externalSkuCode()),
                salesGoods,
                normalizeMultiplier(request.quantityMultiplier()),
                normalizeDefaultUnitPrice(request.defaultUnitPrice()),
                parseMatchType(request.matchType()),
                request.enabled(),
                request.priority(),
                normalizeOptional(request.remark())
        );
        return ChannelProductMappingResponse.from(mappingRepository.save(mapping));
    }

    private SalesChannelConfig findChannel(Long id) {
        return channelRepository.findById(id).orElseThrow(() -> BusinessException.badRequest("销售渠道不存在: " + id));
    }

    private SalesGoods findSalesGoods(Long id) {
        SalesGoods salesGoods = salesGoodsRepository.findWithDetailsById(id).orElseThrow(() -> BusinessException.badRequest("销售商品不存在: " + id));
        if (!Boolean.TRUE.equals(salesGoods.getEnabled())) {
            throw BusinessException.badRequest("销售商品已停用: " + salesGoods.getName());
        }
        return salesGoods;
    }

    private ChannelProductMatchType parseMatchType(String value) {
        try {
            return ChannelProductMatchType.valueOf(normalizeRequired(value).toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest("不支持的匹配类型: " + value);
        }
    }

    private BigDecimal normalizeMultiplier(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessException.badRequest("数量换算倍数必须大于0");
        }
        return value;
    }

    private BigDecimal normalizeDefaultUnitPrice(BigDecimal value) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.badRequest("默认成交价不能小于0");
        }
        return value;
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
