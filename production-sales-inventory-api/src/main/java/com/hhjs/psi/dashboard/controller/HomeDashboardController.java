package com.hhjs.psi.dashboard.controller;

import com.hhjs.psi.common.dto.ApiResponse;
import com.hhjs.psi.dashboard.dto.HomeDashboardResponse;
import com.hhjs.psi.dashboard.service.HomeDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class HomeDashboardController {

    private final HomeDashboardService homeDashboardService;

    public HomeDashboardController(HomeDashboardService homeDashboardService) {
        this.homeDashboardService = homeDashboardService;
    }

    @GetMapping("/home")
    public ApiResponse<HomeDashboardResponse> getHome() {
        return ApiResponse.ok(homeDashboardService.getHome());
    }
}
