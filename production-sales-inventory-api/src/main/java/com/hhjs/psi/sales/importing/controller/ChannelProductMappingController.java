package com.hhjs.psi.sales.importing.controller;

import com.hhjs.psi.sales.importing.dto.ChannelProductMappingRequest;
import com.hhjs.psi.sales.importing.dto.ChannelProductMappingResponse;
import com.hhjs.psi.sales.importing.service.ChannelProductMappingService;
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
@RequestMapping("/api/v1/sales/product-mappings")
public class ChannelProductMappingController {

    private final ChannelProductMappingService service;

    public ChannelProductMappingController(ChannelProductMappingService service) {
        this.service = service;
    }

    @GetMapping
    public List<ChannelProductMappingResponse> getMappings(@RequestParam(required = false) Long channelId) {
        return service.getMappings(channelId);
    }

    @PostMapping
    public ChannelProductMappingResponse create(@Valid @RequestBody ChannelProductMappingRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ChannelProductMappingResponse update(@PathVariable Long id, @Valid @RequestBody ChannelProductMappingRequest request) {
        return service.update(id, request);
    }
}