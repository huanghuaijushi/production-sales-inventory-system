package com.hhjs.psi.sales.importing.controller;

import com.hhjs.psi.sales.importing.dto.SalesChannelConfigRequest;
import com.hhjs.psi.sales.importing.dto.SalesChannelConfigResponse;
import com.hhjs.psi.sales.importing.service.SalesChannelConfigService;
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
@RequestMapping("/api/v1/sales/channels")
public class SalesChannelController {

    private final SalesChannelConfigService service;

    public SalesChannelController(SalesChannelConfigService service) {
        this.service = service;
    }

    @GetMapping
    public List<SalesChannelConfigResponse> getChannels(@RequestParam(defaultValue = "true") boolean enabledOnly) {
        return service.getChannels(enabledOnly);
    }

    @PostMapping
    public SalesChannelConfigResponse create(@Valid @RequestBody SalesChannelConfigRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public SalesChannelConfigResponse update(@PathVariable Long id, @Valid @RequestBody SalesChannelConfigRequest request) {
        return service.update(id, request);
    }
}