package com.kronos.service;

import com.kronos.dto.response.ResponseDTOs;
import com.kronos.entity.Notification;
import com.kronos.entity.User;
import com.kronos.exception.ResourceNotFoundException;
import com.kronos.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserService userService;

    public List<ResponseDTOs.NotificationResponse> getAll(String email) {
        User user = userService.getCurrentUser(email);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ResponseDTOs.NotificationResponse markRead(Long id, String email) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!n.getUser().getEmail().equals(email)) throw new ResourceNotFoundException("Not found");
        n.setRead(true);
        notificationRepository.save(n);
        return mapToResponse(n);
    }

    public long getUnreadCount(String email) {
        User user = userService.getCurrentUser(email);
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    public void createNotification(User user, String message, Notification.Type type) {
        Notification n = new Notification();
        n.setUser(user);
        n.setMessage(message);
        n.setType(type);
        notificationRepository.save(n);
    }

    private ResponseDTOs.NotificationResponse mapToResponse(Notification n) {
        return new ResponseDTOs.NotificationResponse(
                n.getId(), n.getMessage(), n.getType().name(), n.isRead(), n.getCreatedAt());
    }
}
