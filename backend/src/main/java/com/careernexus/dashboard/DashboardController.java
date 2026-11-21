package com.careernexus.dashboard;

import com.careernexus.common.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboard;
    public DashboardController(DashboardService dashboard) { this.dashboard = dashboard; }
    @GetMapping("/summary") public ApiResponse<DashboardDtos.Summary> summary(Authentication auth) { return ApiResponse.ok(dashboard.get(auth.getName())); }
}

