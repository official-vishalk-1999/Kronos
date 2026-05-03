package com.kronos.controller;

import com.kronos.dto.response.ResponseDTOs;
import com.kronos.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired private DashboardService dashboardService;

    @GetMapping("/today-stats")
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.DashboardStats>> todayStats(Authentication auth) {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Today stats",
                dashboardService.getTodayStats(auth.getName())));
    }

    @GetMapping("/weekly-stats")
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.WeeklyStats>> weeklyStats(Authentication auth) {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Weekly stats",
                dashboardService.getWeeklyStats(auth.getName())));
    }
}
