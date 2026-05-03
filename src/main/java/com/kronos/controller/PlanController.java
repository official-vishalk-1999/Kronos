package com.kronos.controller;

import com.kronos.dto.request.RequestDTOs;
import com.kronos.dto.response.ResponseDTOs;
import com.kronos.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    @Autowired private PlanService planService;

    @PostMapping("/generate")
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.AiPlanResponse>> generate(
            Authentication auth, @RequestBody RequestDTOs.GeneratePlanRequest request) {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Plan generated",
                planService.generatePlan(auth.getName(), request)));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.PlanResponse>> confirm(
            Authentication auth, @RequestBody RequestDTOs.ConfirmPlanRequest request) {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Plan confirmed",
                planService.confirmPlan(auth.getName(), request)));
    }

    @GetMapping("/today")
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.PlanResponse>> today(
            Authentication auth) {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Today's plan",
                planService.getTodayPlan(auth.getName())));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.PlanResponse>> byDate(
            Authentication auth,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Plan for date",
                planService.getPlanByDate(auth.getName(), date)));
    }

    @PutMapping("/regenerate")
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.AiPlanResponse>> regenerate(
            Authentication auth, @RequestBody RequestDTOs.GeneratePlanRequest request) {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Plan regenerated",
                planService.regeneratePlan(auth.getName(), request)));
    }

    @DeleteMapping("/today")
    public ResponseEntity<ResponseDTOs.ApiResponse<Void>> deleteTodayPlan(
            Authentication auth) {
        planService.deleteTodayPlan(auth.getName());
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Plan deleted", null));
    }
}