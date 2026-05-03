package com.kronos.controller;

import com.kronos.dto.request.RequestDTOs;
import com.kronos.dto.response.ResponseDTOs;
import com.kronos.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.AuthResponse>> signup(
            @Valid @RequestBody RequestDTOs.SignupRequest request) {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Signup successful", authService.signup(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.AuthResponse>> login(
            @Valid @RequestBody RequestDTOs.LoginRequest request) {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Login successful", authService.login(request)));
    }

    @PostMapping("/admin-login")
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.AuthResponse>> adminLogin(
            @Valid @RequestBody RequestDTOs.AdminLoginRequest request) {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Admin login successful", authService.adminLogin(request)));
    }
}
