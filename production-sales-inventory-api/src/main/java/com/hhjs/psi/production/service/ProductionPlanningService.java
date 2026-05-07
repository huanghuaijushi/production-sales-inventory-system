package com.hhjs.psi.production.service;

import com.hhjs.psi.auth.security.SecurityUtils;
import com.hhjs.psi.common.exception.BusinessException;
import com.hhjs.psi.inventory.dto.StockOperationRequest;
import com.hhjs.psi.inventory.dto.StockRecordResponse;
import com.hhjs.psi.inventory.entity.Product;
import com.hhjs.psi.inventory.entity.ProductType;
import com.hhjs.psi.inventory.entity.Stock;
import com.hhjs.psi.inventory.entity.StockRecordSubType;
import com.hhjs.psi.inventory.entity.StockRecordType;
import com.hhjs.psi.inventory.repository.ProductRepository;
import com.hhjs.psi.inventory.repository.StockRecordRepository;
import com.hhjs.psi.inventory.repository.StockRepository;
import com.hhjs.psi.production.dto.BomItemRequest;
import com.hhjs.psi.production.dto.BomItemResponse;
import com.hhjs.psi.production.dto.ProductionCapacityResponse;
import com.hhjs.psi.production.dto.ProductionInboundRequest;
import com.hhjs.psi.production.dto.ProductionMaterialIssueRequest;
import com.hhjs.psi.production.dto.ProductionMaterialIssueResponse;
import com.hhjs.psi.production.dto.ProductionMaterialPlanResponse;
import com.hhjs.psi.production.dto.ProductionMaterialCapacityResponse;
import com.hhjs.psi.production.dto.ProductionMaterialRequirementResponse;
import com.hhjs.psi.production.dto.ProductionOrderCreateRequest;
import com.hhjs.psi.production.dto.ProductionOrderDetailResponse;
import com.hhjs.psi.production.dto.ProductionOrderSummaryResponse;
import com.hhjs.psi.production.dto.ProductionStepRecordRequest;
import com.hhjs.psi.production.dto.ProductionStepRecordResponse;
import com.hhjs.psi.production.dto.ProductionSuggestionResponse;
import com.hhjs.psi.production.dto.PurchaseSuggestionGroupResponse;
import com.hhjs.psi.production.dto.PurchaseSuggestionItemResponse;
import com.hhjs.psi.production.dto.SupplierMaterialRequest;
import com.hhjs.psi.production.dto.SupplierMaterialResponse;
import com.hhjs.psi.production.entity.BomItem;
import com.hhjs.psi.production.entity.ProductionMaterialIssue;
import com.hhjs.psi.production.entity.ProductionMaterialPlan;
import com.hhjs.psi.production.entity.ProductionOrder;
import com.hhjs.psi.production.entity.ProductionOrderStatus;
import com.hhjs.psi.production.entity.ProductionStepRecord;
import com.hhjs.psi.production.entity.ProductionStepType;
import com.hhjs.psi.production.entity.SupplierMaterial;
import com.hhjs.psi.production.repository.BomItemRepository;
import com.hhjs.psi.production.repository.ProductionMaterialIssueRepository;
import com.hhjs.psi.production.repository.ProductionMaterialPlanRepository;
import com.hhjs.psi.production.repository.ProductionOrderRepository;
import com.hhjs.psi.production.repository.ProductionStepRecordRepository;
import com.hhjs.psi.production.repository.SupplierMaterialRepository;
import com.hhjs.psi.inventory.service.InventoryService;
import com.hhjs.psi.purchase.entity.PurchaseOrderStatus;
import com.hhjs.psi.purchase.repository.PurchaseOrderRepository;
import com.hhjs.psi.supplier.entity.Supplier;
import com.hhjs.psi.supplier.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class ProductionPlanningService {

    private final BomItemRepository bomItemRepository;
    private final SupplierMaterialRepository supplierMaterialRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final StockRecordRepository stockRecordRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InventoryService inventoryService;
    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionMaterialPlanRepository productionMaterialPlanRepository;
    private final ProductionMaterialIssueRepository productionMaterialIssueRepository;
    private final ProductionStepRecordRepository productionStepRecordRepository;

    public ProductionPlanningService(
            BomItemRepository bomItemRepository,
            SupplierMaterialRepository supplierMaterialRepository,
            ProductRepository productRepository,
            StockRepository stockRepository,
            StockRecordRepository stockRecordRepository,
            SupplierRepository supplierRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            InventoryService inventoryService,
            ProductionOrderRepository productionOrderRepository,
            ProductionMaterialPlanRepository productionMaterialPlanRepository,
            ProductionMaterialIssueRepository productionMaterialIssueRepository,
            ProductionStepRecordRepository productionStepRecordRepository
    ) {
        this.bomItemRepository = bomItemRepository;
        this.supplierMaterialRepository = supplierMaterialRepository;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.stockRecordRepository = stockRecordRepository;
        this.supplierRepository = supplierRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.inventoryService = inventoryService;
        this.productionOrderRepository = productionOrderRepository;
        this.productionMaterialPlanRepository = productionMaterialPlanRepository;
        this.productionMaterialIssueRepository = productionMaterialIssueRepository;
        this.productionStepRecordRepository = productionStepRecordRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductionOrderSummaryResponse> getProductionOrders() {
        return productionOrderRepository.findAllWithProduct().stream()
                .map(this::toProductionOrderSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductionOrderDetailResponse getProductionOrder(Long productionOrderId) {
        ProductionOrder order = productionOrderRepository.findByIdWithProduct(productionOrderId)
                .orElseThrow(() -> BusinessException.badRequest("生产工单不存在: " + productionOrderId));
        return toProductionOrderDetailResponse(order);
    }

    @Transactional
    public ProductionOrderDetailResponse createProductionOrder(ProductionOrderCreateRequest request) {
        Product finishedProduct = getEnabledProduct(request.productId(), ProductType.FINISHED_PRODUCT, "成品不存在或已停用");
        List<BomItem> bomItems = bomItemRepository.findByFinishedProductIdWithProducts(finishedProduct.getId());
        if (bomItems.isEmpty()) {
            throw BusinessException.badRequest("请先在生产配置里维护该成品的配方");
        }

        var currentAdmin = SecurityUtils.requireCurrentAdmin();
        ProductionOrder order = ProductionOrder.create(
                generateProductionOrderNo(),
                normalizeProductionBatchNo(finishedProduct, request.batchNo()),
                finishedProduct,
                request.plannedQuantity(),
                request.plannedDate(),
                currentAdmin.id(),
                currentAdmin.username(),
                normalizeOptional(request.remark())
        );
        ProductionOrder savedOrder = productionOrderRepository.save(order);

        List<ProductionMaterialPlan> materialPlans = bomItems.stream()
                .map(item -> ProductionMaterialPlan.create(
                        savedOrder,
                        item.getMaterialProduct(),
                        ceil(BigDecimal.valueOf(request.plannedQuantity()).multiply(requiredPerUnit(item)))
                ))
                .toList();
        productionMaterialPlanRepository.saveAll(materialPlans);

        return toProductionOrderDetailResponse(savedOrder);
    }

    @Transactional
    public ProductionOrderDetailResponse startProductionOrder(Long productionOrderId) {
        ProductionOrder order = getProductionOrderForUpdate(productionOrderId);
        ensureOrderCanOperate(order);
        if (order.getStatus() == ProductionOrderStatus.PLANNED) {
            order.start();
        }
        return toProductionOrderDetailResponse(order);
    }

    @Transactional
    public ProductionOrderDetailResponse issueProductionMaterial(Long productionOrderId, ProductionMaterialIssueRequest request) {
        ProductionOrder order = getProductionOrderForUpdate(productionOrderId);
        ensureOrderCanOperate(order);
        if (order.getStatus() == ProductionOrderStatus.PLANNED) {
            order.start();
        }

        ProductionMaterialPlan plan = productionMaterialPlanRepository.findByIdForUpdate(request.materialPlanId())
                .orElseThrow(() -> BusinessException.badRequest("用料计划不存在: " + request.materialPlanId()));
        if (!plan.getProductionOrder().getId().equals(order.getId())) {
            throw BusinessException.badRequest("用料计划不属于当前生产工单");
        }
        int remainingQuantity = plan.getRequiredQuantity() - plan.getIssuedQuantity();
        if (request.quantity() > remainingQuantity) {
            throw BusinessException.badRequest("领料数量不能超过剩余需求: " + remainingQuantity);
        }

        Product materialProduct = plan.getMaterialProduct();
        StockRecordResponse stockRecord = inventoryService.outbound(new StockOperationRequest(
                materialProduct.getId(),
                StockRecordType.OUT,
                StockRecordSubType.PRODUCTION_USAGE,
                request.quantity(),
                order.getId(),
                null,
                null,
                request.batchId(),
                null,
                null,
                null,
                normalizeOptional(request.remark())
        ));

        plan.addIssued(request.quantity());
        var currentAdmin = SecurityUtils.requireCurrentAdmin();
        productionMaterialIssueRepository.save(ProductionMaterialIssue.create(
                order,
                plan,
                materialProduct,
                request.batchId(),
                stockRecord.batchNo(),
                request.quantity(),
                stockRecord.id(),
                currentAdmin.id(),
                currentAdmin.username(),
                normalizeOptional(request.remark())
        ));

        return toProductionOrderDetailResponse(order);
    }

    @Transactional
    public ProductionOrderDetailResponse recordProductionStep(Long productionOrderId, ProductionStepRecordRequest request) {
        ProductionOrder order = getProductionOrderForUpdate(productionOrderId);
        ensureOrderCanOperate(order);
        if (order.getStatus() == ProductionOrderStatus.PLANNED) {
            order.start();
        }
        if (order.getStatus() == ProductionOrderStatus.WAIT_INBOUND || order.getCurrentStep() == null) {
            throw BusinessException.badRequest("所有工序已完成，请进行成品入库");
        }

        ProductionStepType stepType = order.getCurrentStep();
        int inputQuantity = getCurrentStepInputQuantity(order, stepType);
        if (request.lossQuantity() > inputQuantity) {
            throw BusinessException.badRequest("损耗数量不能超过本步可处理数量: " + inputQuantity);
        }
        int completedQuantity = inputQuantity - request.lossQuantity();

        var currentAdmin = SecurityUtils.requireCurrentAdmin();
        ProductionStepRecord record = ProductionStepRecord.create(
                order,
                stepType,
                completedQuantity,
                request.lossQuantity(),
                normalizeOptional(request.lossReason()),
                currentAdmin.id(),
                currentAdmin.username()
        );
        order.completeStep(completedQuantity, request.lossQuantity(), nextStep(stepType));
        productionStepRecordRepository.save(record);

        return toProductionOrderDetailResponse(order);
    }

    @Transactional
    public ProductionOrderDetailResponse inboundProduction(Long productionOrderId, ProductionInboundRequest request) {
        ProductionOrder order = getProductionOrderForUpdate(productionOrderId);
        ensureOrderCanOperate(order);
        int expectedInbound = Math.max(order.getPlannedQuantity() - order.getLossQuantity(), 0);
        int remainingInbound = Math.max(expectedInbound - order.getInboundQuantity(), 0);
        if (request.quantity() > remainingInbound) {
            throw BusinessException.badRequest("入库数量不能超过待入库数量: " + remainingInbound);
        }

        LocalDate productionDate = request.productionDate() == null ? LocalDate.now() : request.productionDate();
        BigDecimal unitProductionCost = calculateProductionUnitCost(order, request.quantity());
        inventoryService.inbound(new StockOperationRequest(
                order.getProduct().getId(),
                StockRecordType.IN,
                StockRecordSubType.PRODUCTION,
                request.quantity(),
                order.getId(),
                null,
                unitProductionCost,
                null,
                order.getBatchNo(),
                productionDate,
                request.expiryDate(),
                normalizeOptional(request.remark())
        ));
        order.addInbound(request.quantity());

        return toProductionOrderDetailResponse(order);
    }

    private BigDecimal calculateProductionUnitCost(ProductionOrder order, Integer inboundQuantity) {
        if (inboundQuantity == null || inboundQuantity <= 0) {
            throw BusinessException.badRequest("成品入库数量必须大于0");
        }
        BigDecimal materialCostAmount = stockRecordRepository.sumCostAmountByRelatedOrderAndTypeAndSubType(
                "PRODUCTION_ORDER",
                order.getId(),
                StockRecordType.OUT,
                StockRecordSubType.PRODUCTION_USAGE
        );
        return materialCostAmount.divide(BigDecimal.valueOf(inboundQuantity), 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public ProductionOrderDetailResponse cancelProductionOrder(Long productionOrderId) {
        ProductionOrder order = getProductionOrderForUpdate(productionOrderId);
        if (order.getStatus() == ProductionOrderStatus.COMPLETED) {
            throw BusinessException.badRequest("已完成工单不能取消");
        }
        order.cancel();
        return toProductionOrderDetailResponse(order);
    }

    @Transactional(readOnly = true)
    public List<BomItemResponse> getBomItems() {
        return bomItemRepository.findAllEnabledWithProducts().stream()
                .map(this::toBomItemResponse)
                .toList();
    }

    @Transactional
    public BomItemResponse createBomItem(BomItemRequest request) {
        Product finishedProduct = getEnabledProduct(request.finishedProductId(), ProductType.FINISHED_PRODUCT, "成品不存在或已停用");
        Product materialProduct = getEnabledProduct(request.materialProductId(), ProductType.RAW_MATERIAL, "原材料不存在或已停用");
        if (finishedProduct.getId().equals(materialProduct.getId())) {
            throw BusinessException.badRequest("成品不能把自己作为原材料");
        }
        if (bomItemRepository.existsByFinishedProductIdAndMaterialProductId(finishedProduct.getId(), materialProduct.getId())) {
            throw BusinessException.conflict("该成品已经绑定过这个原材料");
        }

        BomItem item = BomItem.create(
                finishedProduct,
                materialProduct,
                normalizeQuantity(request.quantityPerUnit(), "用量必须大于0"),
                normalizeLossRate(request.lossRate())
        );
        return toBomItemResponse(bomItemRepository.save(item));
    }

    @Transactional
    public BomItemResponse updateBomItem(Long bomItemId, BomItemRequest request) {
        BomItem item = bomItemRepository.findByIdAndFinishedProduct_EnabledTrueAndMaterialProduct_EnabledTrue(bomItemId)
                .orElseThrow(() -> BusinessException.badRequest("配方明细不存在: " + bomItemId));
        Product finishedProduct = getEnabledProduct(request.finishedProductId(), ProductType.FINISHED_PRODUCT, "成品不存在或已停用");
        Product materialProduct = getEnabledProduct(request.materialProductId(), ProductType.RAW_MATERIAL, "原材料不存在或已停用");
        if (finishedProduct.getId().equals(materialProduct.getId())) {
            throw BusinessException.badRequest("成品不能把自己作为原材料");
        }
        if (bomItemRepository.existsByFinishedProductIdAndMaterialProductIdAndIdNot(
                finishedProduct.getId(),
                materialProduct.getId(),
                item.getId()
        )) {
            throw BusinessException.conflict("该成品已经绑定过这个原材料");
        }

        item.update(
                finishedProduct,
                materialProduct,
                normalizeQuantity(request.quantityPerUnit(), "用量必须大于0"),
                normalizeLossRate(request.lossRate())
        );
        return toBomItemResponse(bomItemRepository.save(item));
    }

    @Transactional
    public void deleteBomItem(Long bomItemId) {
        BomItem item = bomItemRepository.findByIdAndFinishedProduct_EnabledTrueAndMaterialProduct_EnabledTrue(bomItemId)
                .orElseThrow(() -> BusinessException.badRequest("配方明细不存在: " + bomItemId));
        bomItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public List<SupplierMaterialResponse> getSupplierMaterials() {
        return supplierMaterialRepository.findAllEnabledWithDetails().stream()
                .map(this::toSupplierMaterialResponse)
                .toList();
    }

    @Transactional
    public SupplierMaterialResponse createSupplierMaterial(SupplierMaterialRequest request) {
        Supplier supplier = getEnabledSupplier(request.supplierId());
        Product product = getEnabledProduct(request.productId(), ProductType.RAW_MATERIAL, "原材料不存在或已停用");
        if (supplierMaterialRepository.existsBySupplierIdAndProductId(supplier.getId(), product.getId())) {
            throw BusinessException.conflict("该供应商已经绑定过这个原材料");
        }

        SupplierMaterial material = SupplierMaterial.create(
                supplier,
                product,
                normalizePrice(request.defaultUnitPrice()),
                normalizePositiveInteger(request.minOrderQuantity(), 1, "最小起订量必须大于0"),
                normalizePositiveInteger(request.orderMultiple(), 1, "采购倍数必须大于0"),
                normalizeNonNegativeInteger(request.leadTimeDays(), 0, "交期不能小于0"),
                request.preferred(),
                normalizeOptional(request.remark())
        );
        return toSupplierMaterialResponse(supplierMaterialRepository.save(material));
    }

    @Transactional
    public SupplierMaterialResponse updateSupplierMaterial(Long supplierMaterialId, SupplierMaterialRequest request) {
        SupplierMaterial material = supplierMaterialRepository.findByIdAndSupplier_EnabledTrueAndProduct_EnabledTrue(supplierMaterialId)
                .orElseThrow(() -> BusinessException.badRequest("供货关系不存在: " + supplierMaterialId));

        if (!material.getSupplier().getId().equals(request.supplierId()) || !material.getProduct().getId().equals(request.productId())) {
            throw BusinessException.badRequest("供应商或原材料不支持直接修改，请删除后重新绑定");
        }

        material.update(
                normalizePrice(request.defaultUnitPrice()),
                normalizePositiveInteger(request.minOrderQuantity(), 1, "最小起订量必须大于0"),
                normalizePositiveInteger(request.orderMultiple(), 1, "采购倍数必须大于0"),
                normalizeNonNegativeInteger(request.leadTimeDays(), 0, "交期不能小于0"),
                request.preferred(),
                normalizeOptional(request.remark())
        );
        return toSupplierMaterialResponse(supplierMaterialRepository.save(material));
    }

    @Transactional
    public void deleteSupplierMaterial(Long supplierMaterialId) {
        SupplierMaterial material = supplierMaterialRepository.findByIdAndSupplier_EnabledTrueAndProduct_EnabledTrue(supplierMaterialId)
                .orElseThrow(() -> BusinessException.badRequest("供货关系不存在: " + supplierMaterialId));
        supplierMaterialRepository.delete(material);
    }

    @Transactional(readOnly = true)
    public List<ProductionCapacityResponse> getProductionCapacity() {
        List<Product> finishedProducts = productRepository.findByTypeAndEnabledTrue(ProductType.FINISHED_PRODUCT).stream()
                .sorted(Comparator.comparing(Product::getCode))
                .toList();
        Map<Long, Stock> stockByProductId = getStockByProductId();
        Map<Long, List<BomItem>> bomByFinishedProductId = getBomByFinishedProductId();

        return finishedProducts.stream()
                .map(product -> {
                    Stock stock = stockByProductId.get(product.getId());
                    CapacityCalculation capacity = calculateCapacity(
                            bomByFinishedProductId.getOrDefault(product.getId(), List.of()),
                            stockByProductId
                    );
                    return new ProductionCapacityResponse(
                            product.getId(),
                            product.getCode(),
                            product.getName(),
                            product.getUnit(),
                            quantityOf(stock),
                            availableQuantityOf(stock),
                            product.getAlertQuantity(),
                            !bomByFinishedProductId.getOrDefault(product.getId(), List.of()).isEmpty(),
                            capacity.maxProducibleQuantity(),
                            capacity.bottleneckMaterialName(),
                            capacity.materials()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductionSuggestionResponse> getProductionSuggestions() {
        return buildProductionPlan().suggestions();
    }

    @Transactional(readOnly = true)
    public List<PurchaseSuggestionGroupResponse> getPurchaseSuggestionGroups() {
        ProductionPlan plan = buildProductionPlan();
        Map<String, PurchaseSuggestionGroupBuilder> groups = new LinkedHashMap<>();

        for (ProductionMaterialRequirementResponse requirement : plan.aggregatedRequirements()) {
            if (requirement.shortageQuantity() <= 0 || requirement.suggestedPurchaseQuantity() <= 0) {
                continue;
            }

            String key = requirement.defaultSupplierId() == null
                    ? "UNBOUND"
                    : "SUPPLIER-" + requirement.defaultSupplierId();
            PurchaseSuggestionGroupBuilder group = groups.computeIfAbsent(key, ignored -> new PurchaseSuggestionGroupBuilder(
                    requirement.defaultSupplierId(),
                    requirement.defaultSupplierName() == null ? "未绑定供应商" : requirement.defaultSupplierName()
            ));

            BigDecimal unitPrice = requirement.defaultUnitPrice() == null ? BigDecimal.ZERO : requirement.defaultUnitPrice();
            BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(requirement.suggestedPurchaseQuantity()));
            group.maxLeadTimeDays = Math.max(group.maxLeadTimeDays, requirement.leadTimeDays() == null ? 0 : requirement.leadTimeDays());
            group.totalAmount = group.totalAmount.add(amount);
            group.items.add(new PurchaseSuggestionItemResponse(
                    requirement.materialProductId(),
                    requirement.materialProductCode(),
                    requirement.materialProductName(),
                    requirement.materialProductUnit(),
                    requirement.shortageQuantity(),
                    requirement.suggestedPurchaseQuantity(),
                    requirement.availableQuantity(),
                    requirement.onOrderQuantity(),
                    unitPrice,
                    amount,
                    requirement.leadTimeDays(),
                    requirement.suggestionReason()
            ));
        }

        return groups.values().stream()
                .map(PurchaseSuggestionGroupBuilder::toResponse)
                .toList();
    }

    private ProductionPlan buildProductionPlan() {
        List<Product> finishedProducts = productRepository.findByTypeAndEnabledTrue(ProductType.FINISHED_PRODUCT).stream()
                .sorted(Comparator.comparing(Product::getCode))
                .toList();
        Map<Long, Stock> stockByProductId = getStockByProductId();
        Map<Long, List<BomItem>> bomByFinishedProductId = getBomByFinishedProductId();
        Map<Long, Integer> pendingPurchaseQuantityByProductId = getPendingPurchaseQuantityByProductId();

        Set<Long> materialProductIds = bomByFinishedProductId.values().stream()
                .flatMap(List::stream)
                .map(item -> item.getMaterialProduct().getId())
                .collect(Collectors.toSet());
        Map<Long, SupplierMaterial> preferredSupplierByProductId = getPreferredSupplierByProductId(materialProductIds);

        List<ProductionSuggestionResponse> suggestions = new ArrayList<>();
        Map<Long, MaterialDemand> aggregatedDemandByProductId = new LinkedHashMap<>();

        for (Product finishedProduct : finishedProducts) {
            Stock finishedStock = stockByProductId.get(finishedProduct.getId());
            int currentStock = quantityOf(finishedStock);
            int alertQuantity = nullToZero(finishedProduct.getAlertQuantity());
            int suggestedProductionQuantity = Math.max(alertQuantity - currentStock, 0);
            if (suggestedProductionQuantity <= 0) {
                continue;
            }

            List<BomItem> bomItems = bomByFinishedProductId.getOrDefault(finishedProduct.getId(), List.of());
            if (bomItems.isEmpty()) {
                suggestions.add(new ProductionSuggestionResponse(
                        finishedProduct.getId(),
                        finishedProduct.getCode(),
                        finishedProduct.getName(),
                        finishedProduct.getUnit(),
                        currentStock,
                        availableQuantityOf(finishedStock),
                        alertQuantity,
                        suggestedProductionQuantity,
                        0,
                        false,
                        "未配置成品配方，暂时无法计算可生产数量和原材料缺口。",
                        List.of()
                ));
                continue;
            }

            CapacityCalculation capacity = calculateCapacity(bomItems, stockByProductId);
            List<ProductionMaterialRequirementResponse> requirements = bomItems.stream()
                    .map(item -> {
                        Product materialProduct = item.getMaterialProduct();
                        int requiredQuantity = ceil(BigDecimal.valueOf(suggestedProductionQuantity).multiply(requiredPerUnit(item)));
                        MaterialDemand demand = aggregatedDemandByProductId.computeIfAbsent(
                                materialProduct.getId(),
                                ignored -> new MaterialDemand(materialProduct)
                        );
                        demand.requiredQuantity += requiredQuantity;
                        return toRequirementResponse(
                                materialProduct,
                                requiredQuantity,
                                stockByProductId,
                                pendingPurchaseQuantityByProductId,
                                preferredSupplierByProductId.get(materialProduct.getId())
                        );
                    })
                    .toList();

            boolean canProduceNow = capacity.maxProducibleQuantity() >= suggestedProductionQuantity;
            suggestions.add(new ProductionSuggestionResponse(
                    finishedProduct.getId(),
                    finishedProduct.getCode(),
                    finishedProduct.getName(),
                    finishedProduct.getUnit(),
                    currentStock,
                    availableQuantityOf(finishedStock),
                    alertQuantity,
                    suggestedProductionQuantity,
                    capacity.maxProducibleQuantity(),
                    canProduceNow,
                    canProduceNow
                            ? "现有原材料可以覆盖建议生产量。"
                            : "原材料不足，建议先按缺口生成采购单。",
                    requirements
            ));
        }

        List<ProductionMaterialRequirementResponse> aggregatedRequirements = aggregatedDemandByProductId.values().stream()
                .map(demand -> toRequirementResponse(
                        demand.product,
                        demand.requiredQuantity,
                        stockByProductId,
                        pendingPurchaseQuantityByProductId,
                        preferredSupplierByProductId.get(demand.product.getId())
                ))
                .toList();

        return new ProductionPlan(suggestions, aggregatedRequirements);
    }

    private ProductionMaterialRequirementResponse toRequirementResponse(
            Product materialProduct,
            int requiredQuantity,
            Map<Long, Stock> stockByProductId,
            Map<Long, Integer> pendingPurchaseQuantityByProductId,
            SupplierMaterial supplierMaterial
    ) {
        int availableQuantity = availableQuantityOf(stockByProductId.get(materialProduct.getId()));
        int onOrderQuantity = pendingPurchaseQuantityByProductId.getOrDefault(materialProduct.getId(), 0);
        int shortageQuantity = Math.max(requiredQuantity - availableQuantity - onOrderQuantity, 0);
        int suggestedPurchaseQuantity = shortageQuantity == 0 ? 0 : applyPurchasePolicy(shortageQuantity, supplierMaterial);
        String suggestionReason = buildSuggestionReason(shortageQuantity, suggestedPurchaseQuantity, supplierMaterial);

        return new ProductionMaterialRequirementResponse(
                materialProduct.getId(),
                materialProduct.getCode(),
                materialProduct.getName(),
                materialProduct.getUnit(),
                requiredQuantity,
                availableQuantity,
                onOrderQuantity,
                shortageQuantity,
                supplierMaterial == null ? null : supplierMaterial.getSupplier().getId(),
                supplierMaterial == null ? null : supplierMaterial.getSupplier().getName(),
                supplierMaterial == null ? BigDecimal.ZERO : supplierMaterial.getDefaultUnitPrice(),
                suggestedPurchaseQuantity,
                supplierMaterial == null ? 0 : supplierMaterial.getLeadTimeDays(),
                suggestionReason
        );
    }

    private CapacityCalculation calculateCapacity(List<BomItem> bomItems, Map<Long, Stock> stockByProductId) {
        if (bomItems.isEmpty()) {
            return new CapacityCalculation(0, null, List.of());
        }

        int maxProducibleQuantity = Integer.MAX_VALUE;
        String bottleneckMaterialName = null;
        List<ProductionMaterialCapacityResponse> materialCapacities = new ArrayList<>();

        for (BomItem item : bomItems) {
            Product materialProduct = item.getMaterialProduct();
            int availableQuantity = availableQuantityOf(stockByProductId.get(materialProduct.getId()));
            BigDecimal requiredPerUnit = requiredPerUnit(item);
            int maxSupportQuantity = requiredPerUnit.compareTo(BigDecimal.ZERO) <= 0
                    ? 0
                    : BigDecimal.valueOf(availableQuantity).divide(requiredPerUnit, 0, RoundingMode.FLOOR).intValue();

            if (maxSupportQuantity < maxProducibleQuantity) {
                maxProducibleQuantity = maxSupportQuantity;
                bottleneckMaterialName = materialProduct.getName();
            }

            materialCapacities.add(new ProductionMaterialCapacityResponse(
                    materialProduct.getId(),
                    materialProduct.getCode(),
                    materialProduct.getName(),
                    materialProduct.getUnit(),
                    item.getQuantityPerUnit(),
                    item.getLossRate(),
                    availableQuantity,
                    maxSupportQuantity
            ));
        }

        return new CapacityCalculation(maxProducibleQuantity, bottleneckMaterialName, materialCapacities);
    }

    private Map<Long, Stock> getStockByProductId() {
        return stockRepository.findAllWithProduct().stream()
                .collect(Collectors.toMap(stock -> stock.getProduct().getId(), stock -> stock));
    }

    private Map<Long, List<BomItem>> getBomByFinishedProductId() {
        return bomItemRepository.findAllEnabledWithProducts().stream()
                .collect(Collectors.groupingBy(item -> item.getFinishedProduct().getId(), LinkedHashMap::new, Collectors.toList()));
    }

    private Map<Long, Integer> getPendingPurchaseQuantityByProductId() {
        Map<Long, Integer> pendingQuantityByProductId = new HashMap<>();
        purchaseOrderRepository.sumItemQuantitiesByStatus(PurchaseOrderStatus.PENDING_INBOUND)
                .forEach(row -> pendingQuantityByProductId.put(
                        ((Number) row[0]).longValue(),
                        ((Number) row[1]).intValue()
                ));
        return pendingQuantityByProductId;
    }

    private Map<Long, SupplierMaterial> getPreferredSupplierByProductId(Set<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, SupplierMaterial> preferredSupplierByProductId = new LinkedHashMap<>();
        supplierMaterialRepository.findByProductIdInWithDetails(productIds)
                .forEach(material -> preferredSupplierByProductId.putIfAbsent(material.getProduct().getId(), material));
        return preferredSupplierByProductId;
    }

    private int applyPurchasePolicy(int shortageQuantity, SupplierMaterial supplierMaterial) {
        int minOrderQuantity = supplierMaterial == null ? 1 : Math.max(nullToZero(supplierMaterial.getMinOrderQuantity()), 1);
        int orderMultiple = supplierMaterial == null ? 1 : Math.max(nullToZero(supplierMaterial.getOrderMultiple()), 1);
        int suggestedQuantity = Math.max(shortageQuantity, minOrderQuantity);
        int remainder = suggestedQuantity % orderMultiple;
        return remainder == 0 ? suggestedQuantity : suggestedQuantity + orderMultiple - remainder;
    }

    private String buildSuggestionReason(int shortageQuantity, int suggestedPurchaseQuantity, SupplierMaterial supplierMaterial) {
        if (shortageQuantity <= 0) {
            return "现有库存与待入库采购单可以覆盖生产需求。";
        }
        if (supplierMaterial == null) {
            return "缺口 %d，尚未绑定供应商，建议先维护供货关系。".formatted(shortageQuantity);
        }
        return "缺口 %d，按起订量 %d、采购倍数 %d 建议采购 %d。".formatted(
                shortageQuantity,
                supplierMaterial.getMinOrderQuantity(),
                supplierMaterial.getOrderMultiple(),
                suggestedPurchaseQuantity
        );
    }

    private ProductionOrder getProductionOrderForUpdate(Long productionOrderId) {
        return productionOrderRepository.findByIdForUpdate(productionOrderId)
                .orElseThrow(() -> BusinessException.badRequest("生产工单不存在: " + productionOrderId));
    }

    private void ensureOrderCanOperate(ProductionOrder order) {
        if (order.getStatus() == ProductionOrderStatus.CANCELLED) {
            throw BusinessException.badRequest("已取消工单不能继续操作");
        }
        if (order.getStatus() == ProductionOrderStatus.COMPLETED) {
            throw BusinessException.badRequest("已完成工单不能继续操作");
        }
    }

    private int getCurrentStepInputQuantity(ProductionOrder order, ProductionStepType stepType) {
        if (stepType == ProductionStepType.PREPARATION) {
            return order.getPlannedQuantity();
        }
        return order.getCompletedQuantity();
    }

    private ProductionStepType nextStep(ProductionStepType stepType) {
        return switch (stepType) {
            case PREPARATION -> ProductionStepType.WRAPPING;
            case WRAPPING -> ProductionStepType.COOKING;
            case COOKING -> ProductionStepType.PACKAGING;
            case PACKAGING -> ProductionStepType.STERILIZATION;
            case STERILIZATION -> ProductionStepType.BOXING;
            case BOXING -> null;
        };
    }

    private ProductionOrderDetailResponse toProductionOrderDetailResponse(ProductionOrder order) {
        return new ProductionOrderDetailResponse(
                toProductionOrderSummaryResponse(order),
                productionMaterialPlanRepository.findByProductionOrderIdWithProduct(order.getId()).stream()
                        .map(this::toProductionMaterialPlanResponse)
                        .toList(),
                productionMaterialIssueRepository.findByProductionOrderIdWithDetails(order.getId()).stream()
                        .map(this::toProductionMaterialIssueResponse)
                        .toList(),
                productionStepRecordRepository.findByProductionOrderId(order.getId()).stream()
                        .map(this::toProductionStepRecordResponse)
                        .toList()
        );
    }

    private ProductionOrderSummaryResponse toProductionOrderSummaryResponse(ProductionOrder order) {
        Product product = order.getProduct();
        return new ProductionOrderSummaryResponse(
                order.getId(),
                order.getOrderNo(),
                order.getBatchNo(),
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getUnit(),
                order.getPlannedQuantity(),
                order.getCompletedQuantity(),
                order.getInboundQuantity(),
                order.getLossQuantity(),
                order.getCurrentStep(),
                order.getStatus(),
                order.getPlannedDate(),
                order.getStartedAt(),
                order.getCompletedAt(),
                order.getOperatorName(),
                order.getRemark(),
                order.getCreatedAt()
        );
    }

    private ProductionMaterialPlanResponse toProductionMaterialPlanResponse(ProductionMaterialPlan plan) {
        Product product = plan.getMaterialProduct();
        return new ProductionMaterialPlanResponse(
                plan.getId(),
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getUnit(),
                plan.getRequiredQuantity(),
                plan.getIssuedQuantity()
        );
    }

    private ProductionMaterialIssueResponse toProductionMaterialIssueResponse(ProductionMaterialIssue issue) {
        Product product = issue.getMaterialProduct();
        return new ProductionMaterialIssueResponse(
                issue.getId(),
                issue.getMaterialPlan().getId(),
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getUnit(),
                issue.getStockBatchId(),
                issue.getBatchNo(),
                issue.getIssuedQuantity(),
                issue.getStockRecordId(),
                issue.getOperatorName(),
                issue.getRemark(),
                issue.getCreatedAt()
        );
    }

    private ProductionStepRecordResponse toProductionStepRecordResponse(ProductionStepRecord record) {
        return new ProductionStepRecordResponse(
                record.getId(),
                record.getStepType(),
                record.getCompletedQuantity(),
                record.getLossQuantity(),
                record.getLossReason(),
                record.getOperatorName(),
                record.getCreatedAt()
        );
    }

    private String generateProductionOrderNo() {
        String timestamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "MO%s%d".formatted(timestamp, random);
    }

    private String normalizeProductionBatchNo(Product product, String batchNo) {
        if (batchNo != null && !batchNo.isBlank()) {
            return batchNo.trim();
        }
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return "%s-%s-%d".formatted(product.getCode(), date, random);
    }

    private BomItemResponse toBomItemResponse(BomItem item) {
        Product finishedProduct = item.getFinishedProduct();
        Product materialProduct = item.getMaterialProduct();
        return new BomItemResponse(
                item.getId(),
                finishedProduct.getId(),
                finishedProduct.getCode(),
                finishedProduct.getName(),
                materialProduct.getId(),
                materialProduct.getCode(),
                materialProduct.getName(),
                materialProduct.getUnit(),
                item.getQuantityPerUnit(),
                item.getLossRate()
        );
    }

    private SupplierMaterialResponse toSupplierMaterialResponse(SupplierMaterial material) {
        Product product = material.getProduct();
        Supplier supplier = material.getSupplier();
        return new SupplierMaterialResponse(
                material.getId(),
                supplier.getId(),
                supplier.getName(),
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getUnit(),
                material.getDefaultUnitPrice(),
                material.getMinOrderQuantity(),
                material.getOrderMultiple(),
                material.getLeadTimeDays(),
                material.getPreferred(),
                material.getRemark()
        );
    }

    private Product getEnabledProduct(Long productId, ProductType expectedType, String notFoundMessage) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> BusinessException.badRequest(notFoundMessage + ": " + productId));
        if (!Boolean.TRUE.equals(product.getEnabled()) || product.getType() != expectedType) {
            throw BusinessException.badRequest(notFoundMessage + ": " + productId);
        }
        return product;
    }

    private Supplier getEnabledSupplier(Long supplierId) {
        return supplierRepository.findByIdAndEnabledTrue(supplierId)
                .orElseThrow(() -> BusinessException.badRequest("供应商不存在或已停用: " + supplierId));
    }

    private BigDecimal requiredPerUnit(BomItem item) {
        BigDecimal lossRate = item.getLossRate() == null ? BigDecimal.ZERO : item.getLossRate();
        return item.getQuantityPerUnit().multiply(BigDecimal.ONE.add(lossRate));
    }

    private int ceil(BigDecimal value) {
        return value.setScale(0, RoundingMode.CEILING).intValue();
    }

    private BigDecimal normalizeQuantity(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessException.badRequest(message);
        }
        return value;
    }

    private BigDecimal normalizeLossRate(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw BusinessException.badRequest("损耗率必须在0到1之间");
        }
        return value;
    }

    private BigDecimal normalizePrice(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.badRequest("默认单价不能小于0");
        }
        return value;
    }

    private Integer normalizePositiveInteger(Integer value, int defaultValue, String message) {
        int normalizedValue = value == null ? defaultValue : value;
        if (normalizedValue <= 0) {
            throw BusinessException.badRequest(message);
        }
        return normalizedValue;
    }

    private Integer normalizeNonNegativeInteger(Integer value, int defaultValue, String message) {
        int normalizedValue = value == null ? defaultValue : value;
        if (normalizedValue < 0) {
            throw BusinessException.badRequest(message);
        }
        return normalizedValue;
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private int quantityOf(Stock stock) {
        return stock == null ? 0 : nullToZero(stock.getQuantity());
    }

    private int availableQuantityOf(Stock stock) {
        return stock == null ? 0 : nullToZero(stock.getAvailableQuantity());
    }

    private record CapacityCalculation(
            Integer maxProducibleQuantity,
            String bottleneckMaterialName,
            List<ProductionMaterialCapacityResponse> materials
    ) {
    }

    private record ProductionPlan(
            List<ProductionSuggestionResponse> suggestions,
            List<ProductionMaterialRequirementResponse> aggregatedRequirements
    ) {
    }

    private static class MaterialDemand {
        private final Product product;
        private int requiredQuantity;

        private MaterialDemand(Product product) {
            this.product = product;
        }
    }

    private static class PurchaseSuggestionGroupBuilder {
        private final Long supplierId;
        private final String supplierName;
        private final List<PurchaseSuggestionItemResponse> items = new ArrayList<>();
        private BigDecimal totalAmount = BigDecimal.ZERO;
        private int maxLeadTimeDays;

        private PurchaseSuggestionGroupBuilder(Long supplierId, String supplierName) {
            this.supplierId = supplierId;
            this.supplierName = supplierName;
        }

        private PurchaseSuggestionGroupResponse toResponse() {
            return new PurchaseSuggestionGroupResponse(
                    supplierId,
                    supplierName,
                    supplierId == null ? null : LocalDate.now().plusDays(maxLeadTimeDays),
                    totalAmount,
                    items
            );
        }
    }
}
