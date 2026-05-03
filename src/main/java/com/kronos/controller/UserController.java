package com.kronos.controller;

import com.kronos.dto.request.RequestDTOs;
import com.kronos.dto.response.ResponseDTOs;
import com.kronos.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.UserProfileResponse>> getProfile(Authentication auth) {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Profile fetched",
                userService.getProfile(auth.getName())));
    }

    @PutMapping("/settings")
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.UserProfileResponse>> updateSettings(
            Authentication auth, @RequestBody RequestDTOs.UpdateSettingsRequest request) {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Settings updated",
                userService.updateSettings(auth.getName(), request)));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ResponseDTOs.ApiResponse<Void>> changePassword(
            Authentication auth, @RequestBody RequestDTOs.ChangePasswordRequest request) {
        userService.changePassword(auth.getName(), request);
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Password changed successfully", null));
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<ResponseDTOs.ApiResponse<Void>> deleteAccount(Authentication auth) {
        userService.deleteAccount(auth.getName());
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Account deleted successfully", null));
    }
}