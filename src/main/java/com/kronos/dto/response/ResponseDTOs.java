package com.kronos.dto.response;

import com.kronos.entity.Task;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ResponseDTOs {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> ApiResponse<T> ok(String message, T data) {
            return new ApiResponse<>(true, message, data);
        }

        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, message, null);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthResponse {
        private String token;
        private String role;
        private String name;
        private String email;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserProfileResponse {
        private Long id;
        private String name;
        private String email;
        private String profession;
        private String dayStartTime;
        private String dayEndTime;
        private boolean morningFocus;
        private int breakDuration;
        private boolean taskReminders;
        private boolean aiSuggestions;
        private boolean missedAlerts;
        private boolean weeklyReport;
        private String role;
        private LocalDateTime createdAt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TaskResponse {
        private Long id;
        private String title;
        private String startTime;
        private String endTime;
        private Task.Status status;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlanResponse {
        private Long id;
        private LocalDate date;
        private List<TaskResponse> tasks;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AiPlanResponse {
        private String message;
        private List<AiTask> tasks;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AiTask {
        private String title;
        private String startTime;
        private String endTime;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotificationResponse {
        private Long id;
        private String message;
        private String type;
        private boolean read;
        private LocalDateTime createdAt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DashboardStats {
        private int totalTasks;
        private int completedTasks;
        private int missedTasks;
        private int pendingTasks;
        private int inProgressTasks;
        private double productivityPercentage;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeeklyStats {
        private List<String> labels;
        private List<Double> productivity;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminUserResponse {
        private Long id;
        private String name;
        private String email;
        private boolean enabled;
        private LocalDateTime createdAt;
        private long plansCount;
        private String role;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminStats {
        private long totalUsers;
        private long activeUsers;
        private long disabledUsers;
        private long totalPlans;
        private long totalTasks;
    }
}
