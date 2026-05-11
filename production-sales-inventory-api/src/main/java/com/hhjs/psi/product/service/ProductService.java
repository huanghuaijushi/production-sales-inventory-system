package com.hhjs.psi.product.service;

import com.hhjs.psi.common.exception.BusinessException;
import com.hhjs.psi.inventory.entity.Product;
import com.hhjs.psi.inventory.entity.ProductType;
import com.hhjs.psi.inventory.entity.Stock;
import com.hhjs.psi.inventory.repository.ProductRepository;
import com.hhjs.psi.inventory.repository.StockRepository;
import com.hhjs.psi.product.dto.ProductRequest;
import com.hhjs.psi.product.dto.ProductResponse;
import com.hhjs.psi.product.repository.ProductCategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public ProductService(
            ProductRepository productRepository,
            StockRepository stockRepository,
            ProductCategoryRepository productCategoryRepository
    ) {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"));
        return productRepository.findByEnabledTrue(pageable)
                .map(this::toResponse);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        String code = request.getCode().trim();
        if (productRepository.existsByCode(code)) {
            throw BusinessException.conflict("产品编码已存在: " + code);
        }

        ProductType productType = parseProductType(request.getType());
        String category = validateCategory(request.getCategory(), productType);

        Product product = Product.create(
                code,
                request.getName().trim(),
                productType,
                category,
                normalizeOptional(request.getSpecification()),
                request.getUnit().trim(),
                toBigDecimal(request.getCostPrice()),
                request.getAlertQuantity(),
                normalizeOptional(request.getDescription())
        );

        Product savedProduct = productRepository.save(product);
        stockRepository.save(Stock.create(savedProduct));

        return toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> BusinessException.badRequest("产品不存在: " + productId));
        if (!Boolean.TRUE.equals(product.getEnabled())) {
            throw BusinessException.badRequest("产品已停用");
        }

        String code = request.getCode().trim();
        if (productRepository.existsByCodeAndIdNot(code, productId)) {
            throw BusinessException.conflict("产品编码已存在: " + code);
        }

        ProductType productType = parseProductType(request.getType());
        String category = validateCategory(request.getCategory(), productType);

        product.updateBasicInfo(
                code,
                request.getName().trim(),
                productType,
                category,
                normalizeOptional(request.getSpecification()),
                request.getUnit().trim(),
                toBigDecimal(request.getCostPrice()),
                request.getAlertQuantity(),
                normalizeOptional(request.getDescription())
        );

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> BusinessException.badRequest("产品不存在: " + productId));
        if (!Boolean.TRUE.equals(product.getEnabled())) {
            return;
        }

        stockRepository.findByProductId(productId).ifPresent(stock -> {
            if (stock.getQuantity() > 0 || stock.getLockedQuantity() > 0) {
                throw BusinessException.badRequest("产品仍有库存或锁定库存，不能删除");
            }
        });

        product.disable();
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"));
        return productRepository.searchEnabledProducts(query.trim(), pageable)
                .map(this::toResponse);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getType().name(),
                product.getCategory(),
                product.getUnit(),
                product.getSpecification(),
                toDouble(product.getCostPrice()),
                product.getAlertQuantity(),
                product.getDescription()
        );
    }

    private ProductType parseProductType(String type) {
        return switch (type) {
            case "FINISHED_PRODUCT", "成品", "半成品" -> ProductType.FINISHED_PRODUCT;
            case "RAW_MATERIAL", "原料", "包装" -> ProductType.RAW_MATERIAL;
            default -> throw BusinessException.badRequest("不支持的产品类型: " + type);
        };
    }

    private String validateCategory(String category, ProductType productType) {
        String normalizedCategory = normalizeOptional(category);
        if (normalizedCategory == null) {
            return null;
        }
        boolean exists = productCategoryRepository.findEnabledByOptionalType(productType).stream()
                .anyMatch(item -> item.getName().equals(normalizedCategory));
        if (!exists) {
            throw BusinessException.badRequest("分类不存在或未启用: " + normalizedCategory);
        }
        return normalizedCategory;
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
