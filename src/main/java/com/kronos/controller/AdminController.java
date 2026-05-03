package com.kronos.controller;

import com.kronos.dto.response.ResponseDTOs;
import com.kronos.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<ResponseDTOs.ApiResponse<List<ResponseDTOs.AdminUserResponse>>> getUsers() {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Users fetched", adminService.getAllUsers()));
    }

    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.AdminUserResponse>> toggleUser(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("User toggled", adminService.toggleUser(id)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ResponseDTOs.ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("User deleted", null));
    }

    @GetMapping("/stats")
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.AdminStats>> getStats() {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Stats fetched", adminService.getStats()));
    }
}
