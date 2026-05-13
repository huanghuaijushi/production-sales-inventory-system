package com.hhjs.psi.sales.goods.service;

import com.hhjs.psi.common.exception.BusinessException;
import com.hhjs.psi.inventory.entity.Product;
import com.hhjs.psi.inventory.repository.ProductRepository;
import com.hhjs.psi.sales.goods.dto.SalesGoodsRequest;
import com.hhjs.psi.sales.goods.dto.SalesGoodsResponse;
import com.hhjs.psi.sales.goods.dto.SalesSkuRequest;
import com.hhjs.psi.sales.goods.dto.SalesSkuResponse;
import com.hhjs.psi.sales.goods.entity.SalesGoods;
import com.hhjs.psi.sales.goods.entity.SalesGoodsChannelPrice;
import com.hhjs.psi.sales.goods.entity.SalesGoodsComponent;
import com.hhjs.psi.sales.goods.entity.SalesSku;
import com.hhjs.psi.sales.goods.entity.SalesSkuComponent;
import com.hhjs.psi.sales.goods.repository.SalesGoodsRepository;
import com.hhjs.psi.sales.goods.repository.SalesSkuComponentRepository;
import com.hhjs.psi.sales.goods.repository.SalesSkuRepository;
import com.hhjs.psi.sales.importing.entity.SalesChannelConfig;
import com.hhjs.psi.sales.importing.repository.SalesChannelConfigRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SalesGoodsService {

    private final SalesGoodsRepository salesGoodsRepository;
    private final SalesSkuRepository salesSkuRepository;
    private final SalesSkuComponentRepository salesSkuComponentRepository;
    private final ProductRepository productRepository;
    private final SalesChannelConfigRepository channelRepository;

    public SalesGoodsService(SalesGoodsRepository salesGoodsRepository, SalesSkuRepository salesSkuRepository, SalesSkuComponentRepository salesSkuComponentRepository, ProductRepository productRepository, SalesChannelConfigRepository channelRepository) {
        this.salesGoodsRepository = salesGoodsRepository;
        this.salesSkuRepository = salesSkuRepository;
        this.salesSkuComponentRepository = salesSkuComponentRepository;
        this.productRepository = productRepository;
        this.channelRepository = channelRepository;
    }

    @Transactional(readOnly = true)
    public Page<SalesGoodsResponse> getGoods(int page, int size, String query) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"));
        Page<SalesGoods> goodsPage = query == null || query.isBlank()
                ? salesGoodsRepository.findByEnabledTrue(pageable)
                : salesGoodsRepository.searchEnabledGoods(query.trim(), pageable);
        return goodsPage.map(SalesGoodsResponse::from);
    }

    @Transactional(readOnly = true)
    public List<SalesGoodsResponse> getEnabledGoods() {
        return salesGoodsRepository.findByEnabledTrueOrderByCodeAsc().stream().map(SalesGoodsResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SalesGoodsResponse getGoodsDetail(Long id) {
        return SalesGoodsResponse.from(findGoods(id));
    }

    @Transactional
    public SalesGoodsResponse create(SalesGoodsRequest request) {
        String code = normalizeRequired(request.code(), "商品编码不能为空");
        if (salesGoodsRepository.existsByCode(code)) {
            throw BusinessException.conflict("商品编码已存在: " + code);
        }
        SalesGoods goods = SalesGoods.create(
                code,
                normalizeRequired(request.name(), "商品名称不能为空"),
                normalizeOptional(request.category()),
                normalizeOptional(request.specification()),
                normalizeRequired(request.unit(), "销售单位不能为空"),
                normalizeMoney(request.defaultPrice()),
                normalizeOptional(request.remark())
        );
        goods.updateBasicInfo(goods.getCode(), goods.getName(), goods.getCategory(), goods.getSpecification(), goods.getUnit(), goods.getDefaultPrice(), request.enabled(), goods.getRemark());
        goods.replaceComponents(buildComponents(request.components()));
        goods.replaceChannelPrices(buildChannelPrices(request.channelPrices()));
        return SalesGoodsResponse.from(salesGoodsRepository.save(goods));
    }

    @Transactional
    public SalesGoodsResponse update(Long id, SalesGoodsRequest request) {
        SalesGoods goods = findGoods(id);
        String code = normalizeRequired(request.code(), "商品编码不能为空");
        if (salesGoodsRepository.existsByCodeAndIdNot(code, id)) {
            throw BusinessException.conflict("商品编码已存在: " + code);
        }
        goods.updateBasicInfo(
                code,
                normalizeRequired(request.name(), "商品名称不能为空"),
                normalizeOptional(request.category()),
                normalizeOptional(request.specification()),
                normalizeRequired(request.unit(), "销售单位不能为空"),
                normalizeMoney(request.defaultPrice()),
                request.enabled(),
                normalizeOptional(request.remark())
        );
        goods.replaceComponents(buildComponents(request.components()));
        goods.replaceChannelPrices(buildChannelPrices(request.channelPrices()));
        return SalesGoodsResponse.from(salesGoodsRepository.save(goods));
    }

    @Transactional(readOnly = true)
    public List<SalesSkuResponse> getSkus() {
        return salesSkuRepository.findByEnabledTrue().stream().map(SalesSkuResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SalesSkuResponse> getSkusByGoods(Long salesGoodsId) {
        return salesSkuRepository.findBySalesGoodsIdAndEnabledTrue(salesGoodsId).stream()
                .map(SalesSkuResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SalesSkuResponse getSkuDetail(Long id) {
        return SalesSkuResponse.from(findSku(id));
    }

    @Transactional
    public SalesSkuResponse createSku(SalesSkuRequest request) {
        String code = normalizeRequired(request.code(), "SKU编码不能为空");
        if (salesSkuRepository.existsByCode(code)) {
            throw BusinessException.conflict("SKU编码已存在: " + code);
        }
        SalesGoods goods = null;
        if (request.salesGoodsId() != null) {
            goods = salesGoodsRepository.findById(request.salesGoodsId())
                    .orElseThrow(() -> BusinessException.badRequest("销售商品不存在: " + request.salesGoodsId()));
        }
        SalesSku sku = SalesSku.create(
                code,
                normalizeRequired(request.name(), "SKU名称不能为空"),
                normalizeOptional(request.specName()),
                normalizeRequired(request.unit(), "销售单位不能为空"),
                normalizeMoney(request.perSkuPrice()),
                normalizeOptional(request.remark())
        );
        if (goods != null) {
            sku.attachToGoods(goods);
        }
        sku.updateBasicInfo(sku.getCode(), sku.getName(), sku.getSpecName(), sku.getUnit(), sku.getPerSkuPrice(), request.enabled(), sku.getRemark());
        sku.replaceComponents(buildSkuComponents(request.components()));
        return SalesSkuResponse.from(salesSkuRepository.save(sku));
    }

    @Transactional
    public SalesSkuResponse updateSku(Long id, SalesSkuRequest request) {
        SalesSku sku = findSku(id);
        String code = normalizeRequired(request.code(), "SKU编码不能为空");
        if (salesSkuRepository.existsByCodeAndIdNot(code, id)) {
            throw BusinessException.conflict("SKU编码已存在: " + code);
        }
        SalesGoods goods = null;
        if (request.salesGoodsId() != null) {
            goods = salesGoodsRepository.findById(request.salesGoodsId())
                    .orElseThrow(() -> BusinessException.badRequest("销售商品不存在: " + request.salesGoodsId()));
        }
        if (goods != null) {
            sku.attachToGoods(goods);
        } else {
            sku.attachToGoods(null);
        }
        sku.updateBasicInfo(
                code,
                normalizeRequired(request.name(), "SKU名称不能为空"),
                normalizeOptional(request.specName()),
                normalizeRequired(request.unit(), "销售单位不能为空"),
                normalizeMoney(request.perSkuPrice()),
                request.enabled(),
                normalizeOptional(request.remark())
        );
        sku.replaceComponents(buildSkuComponents(request.components()));
        return SalesSkuResponse.from(salesSkuRepository.save(sku));
    }

    @Transactional
    public void deleteSku(Long id) {
        SalesSku sku = findSku(id);
        salesSkuRepository.delete(sku);
    }

    private SalesGoods findGoods(Long id) {
        return salesGoodsRepository.findWithDetailsById(id)
                .orElseThrow(() -> BusinessException.badRequest("销售商品不存在: " + id));
    }

    private SalesSku findSku(Long id) {
        return salesSkuRepository.findWithDetailsById(id)
                .orElseThrow(() -> BusinessException.badRequest("销售SKU不存在: " + id));
    }

    private List<SalesGoodsComponent> buildComponents(List<SalesGoodsRequest.ComponentRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw BusinessException.badRequest("商品组成不能为空");
        }
        Set<Long> productIds = new HashSet<>();
        return requests.stream().map(request -> {
            if (!productIds.add(request.productId())) {
                throw BusinessException.badRequest("同一个销售商品不能重复配置同一个库存产品");
            }
            Product product = productRepository.findById(request.productId())
                    .orElseThrow(() -> BusinessException.badRequest("库存产品不存在: " + request.productId()));
            if (!Boolean.TRUE.equals(product.getEnabled())) {
                throw BusinessException.badRequest("库存产品已停用: " + product.getName());
            }
            BigDecimal quantity = request.quantityPerUnit();
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw BusinessException.badRequest("组成数量必须大于0");
            }
            BigDecimal lossRate = request.lossRate() == null ? BigDecimal.ZERO : request.lossRate();
            if (lossRate.compareTo(BigDecimal.ZERO) < 0 || lossRate.compareTo(BigDecimal.ONE) > 0) {
                throw BusinessException.badRequest("损耗率必须在0到1之间");
            }
            return SalesGoodsComponent.create(product, quantity, lossRate, normalizeOptional(request.remark()));
        }).toList();
    }

    private List<SalesSkuComponent> buildSkuComponents(List<SalesSkuRequest.ComponentRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        Set<Long> productIds = new HashSet<>();
        return requests.stream().map(request -> {
            if (!productIds.add(request.productId())) {
                throw BusinessException.badRequest("同一个SKU不能重复配置同一个库存产品");
            }
            Product product = productRepository.findById(request.productId())
                    .orElseThrow(() -> BusinessException.badRequest("库存产品不存在: " + request.productId()));
            BigDecimal quantity = request.quantity();
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw BusinessException.badRequest("组成数量必须大于0");
            }
            return SalesSkuComponent.create(product, quantity, normalizeOptional(request.remark()));
        }).toList();
    }

    private List<SalesGoodsChannelPrice> buildChannelPrices(List<SalesGoodsRequest.ChannelPriceRequest> requests) {
        if (requests == null) {
            return List.of();
        }
        Set<Long> channelIds = new HashSet<>();
        return requests.stream().map(request -> {
            if (!channelIds.add(request.channelId())) {
                throw BusinessException.badRequest("同一个销售商品不能重复配置同一个渠道价格");
            }
            SalesChannelConfig channel = channelRepository.findById(request.channelId())
                    .orElseThrow(() -> BusinessException.badRequest("销售渠道不存在: " + request.channelId()));
            return SalesGoodsChannelPrice.create(channel, normalizeMoney(request.price()), request.enabled(), normalizeOptional(request.remark()));
        }).toList();
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.badRequest("金额不能小于0");
        }
        return value;
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw BusinessException.badRequest(message);
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
