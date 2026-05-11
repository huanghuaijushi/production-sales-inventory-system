package com.hhjs.psi.sales.goods.controller;

import com.hhjs.psi.common.dto.ApiResponse;
import com.hhjs.psi.common.dto.PageResponse;
import com.hhjs.psi.sales.goods.dto.SalesGoodsRequest;
import com.hhjs.psi.sales.goods.dto.SalesGoodsResponse;
import com.hhjs.psi.sales.goods.service.SalesGoodsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales-goods")
public class SalesGoodsController {

    private final SalesGoodsService salesGoodsService;

    public SalesGoodsController(SalesGoodsService salesGoodsService) {
        this.salesGoodsService = salesGoodsService;
    }

    @GetMapping
    public ApiResponse<PageResponse<SalesGoodsResponse>> getGoods(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String query
    ) {
        return ApiResponse.ok(PageResponse.from(salesGoodsService.getGoods(page, size, query)));
    }

    @GetMapping("/enabled")
    public ApiResponse<List<SalesGoodsResponse>> getEnabledGoods() {
        return ApiResponse.ok(salesGoodsService.getEnabledGoods());
    }

    @GetMapping("/{id}")
    public ApiResponse<SalesGoodsResponse> getGoodsDetail(@PathVariable Long id) {
        return ApiResponse.ok(salesGoodsService.getGoodsDetail(id));
    }

    @PostMapping
    public ApiResponse<SalesGoodsResponse> create(@Valid @RequestBody SalesGoodsRequest request) {
        return ApiResponse.ok(salesGoodsService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<SalesGoodsResponse> update(@PathVariable Long id, @Valid @RequestBody SalesGoodsRequest request) {
        return ApiResponse.ok(salesGoodsService.update(id, request));
    }
}
