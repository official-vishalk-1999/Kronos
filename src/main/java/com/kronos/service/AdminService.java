package com.kronos.service;

import com.kronos.dto.request.RequestDTOs;
import com.kronos.dto.response.ResponseDTOs;
import com.kronos.entity.User;
import com.kronos.exception.ResourceNotFoundException;
import com.kronos.exception.ValidationException;
import com.kronos.repository.DayPlanRepository;
import com.kronos.repository.TaskRepository;
import com.kronos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired private UserRepository userRepository;
    @Autowired private DayPlanRepository dayPlanRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public List<ResponseDTOs.AdminUserResponse> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToAdminUser).collect(Collectors.toList());
    }

    public ResponseDTOs.AdminUserResponse toggleUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return mapToAdminUser(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }

    public ResponseDTOs.AdminStats getStats() {
        List<User> users = userRepository.findAll();
        long total = users.size();
        long active = users.stream().filter(User::isEnabled).count();
        long disabled = total - active;
        long totalPlans = dayPlanRepository.count();
        long totalTasks = taskRepository.count();
        return new ResponseDTOs.AdminStats(total, active, disabled, totalPlans, totalTasks);
    }

    private ResponseDTOs.AdminUserResponse mapToAdminUser(User user) {
        long plans = dayPlanRepository.countByUser(user);
        return new ResponseDTOs.AdminUserResponse(
                user.getId(), user.getName(), user.getEmail(),
                user.isEnabled(), user.getCreatedAt(), plans, user.getRole().name());
    }
}
