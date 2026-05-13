package com.hhjs.psi.sales.goods.controller;

import com.hhjs.psi.sales.goods.dto.SalesGoodsRequest;
import com.hhjs.psi.sales.goods.dto.SalesGoodsResponse;
import com.hhjs.psi.sales.goods.dto.SalesSkuRequest;
import com.hhjs.psi.sales.goods.dto.SalesSkuResponse;
import com.hhjs.psi.sales.goods.service.SalesGoodsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales-goods")
public class SalesGoodsController {

    private final SalesGoodsService service;

    public SalesGoodsController(SalesGoodsService service) {
        this.service = service;
    }

    @GetMapping
    public List<SalesGoodsResponse> list() {
        return service.getEnabledGoods();
    }

    @GetMapping("/enabled")
    public List<SalesGoodsResponse> getEnabledGoods() {
        return service.getEnabledGoods();
    }

    @GetMapping("/{id}")
    public SalesGoodsResponse get(@PathVariable Long id) {
        return service.getGoodsDetail(id);
    }

    @PostMapping
    public SalesGoodsResponse create(@Valid @RequestBody SalesGoodsRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public SalesGoodsResponse update(@PathVariable Long id, @Valid @RequestBody SalesGoodsRequest request) {
        return service.update(id, request);
    }

    @GetMapping("/{goodsId}/skus")
    public List<SalesSkuResponse> getSkus(@PathVariable Long goodsId) {
        return service.getSkusByGoods(goodsId);
    }

    @GetMapping("/{goodsId}/skus/{skuId}")
    public SalesSkuResponse getSkuDetail(@PathVariable Long goodsId, @PathVariable Long skuId) {
        return service.getSkuDetail(skuId);
    }

    @PostMapping("/{goodsId}/skus")
    public SalesSkuResponse createSku(@PathVariable Long goodsId, @Valid @RequestBody SalesSkuRequest request) {
        return service.createSku(request);
    }

    @PutMapping("/{goodsId}/skus/{skuId}")
    public SalesSkuResponse updateSku(@PathVariable Long goodsId, @PathVariable Long skuId, @Valid @RequestBody SalesSkuRequest request) {
        return service.updateSku(skuId, request);
    }

    @DeleteMapping("/{goodsId}/skus/{skuId}")
    public void deleteSku(@PathVariable Long goodsId, @PathVariable Long skuId) {
        service.deleteSku(skuId);
    }
}
