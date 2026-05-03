package com.kronos.controller;

import com.kronos.dto.response.ResponseDTOs;
import com.kronos.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ResponseDTOs.ApiResponse<List<ResponseDTOs.NotificationResponse>>> getAll(Authentication auth) {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Notifications fetched",
                notificationService.getAll(auth.getName())));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.NotificationResponse>> markRead(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Marked as read",
                notificationService.markRead(id, auth.getName())));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ResponseDTOs.ApiResponse<Map<String, Long>>> unreadCount(Authentication auth) {
        long count = notificationService.getUnreadCount(auth.getName());
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Unread count", Map.of("count", count)));
    }
}
