package com.kronos.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

public class RequestDTOs {

    @Data
    public static class SignupRequest {
        @NotBlank @Size(min = 2, message = "Name must be at least 2 characters")
        private String name;

        @NotBlank @Email(message = "Invalid email format")
        private String email;

        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = ".*\\d.*", message = "Password must contain at least one number")
        private String password;
    }

    @Data
    public static class LoginRequest {
        @NotBlank private String email;
        @NotBlank private String password;
    }

    @Data
    public static class AdminLoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }

    @Data
    public static class UpdateSettingsRequest {
        private String profession;
        private String dayStartTime;
        private String dayEndTime;
        private Boolean morningFocus;
        private Integer breakDuration;
        private Boolean taskReminders;
        private Boolean aiSuggestions;
        private Boolean missedAlerts;
        private Boolean weeklyReport;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank private String currentPassword;
        @NotBlank @Size(min = 8) @Pattern(regexp = ".*\\d.*")
        private String newPassword;
    }

    @Data
    public static class GeneratePlanRequest {
        // message can be empty when history is provided — remove @NotBlank
        private String message;
        private java.util.List<java.util.Map<String, String>> history;
    }

    @Data
    public static class ConfirmPlanRequest {
        private java.util.List<TaskRequest> tasks;
    }

    @Data
    public static class TaskRequest {
        @NotBlank private String title;
        @NotBlank private String startTime;
        @NotBlank private String endTime;
    }

    @Data
    public static class UpdateTaskRequest {
        private String title;
        private String startTime;
        private String endTime;
    }
}
