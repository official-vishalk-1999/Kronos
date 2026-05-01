package com.kronos.service;

import com.kronos.dto.request.RequestDTOs;
import com.kronos.dto.response.ResponseDTOs;
import com.kronos.entity.User;
import com.kronos.exception.ResourceNotFoundException;
import com.kronos.exception.ValidationException;
import com.kronos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public ResponseDTOs.UserProfileResponse getProfile(String email) {
        User user = getCurrentUser(email);
        return mapToProfile(user);
    }

    public ResponseDTOs.UserProfileResponse updateSettings(String email, RequestDTOs.UpdateSettingsRequest req) {
        User user = getCurrentUser(email);
        if (req.getProfession() != null)    user.setProfession(req.getProfession());
        if (req.getDayStartTime() != null)  user.setDayStartTime(req.getDayStartTime());
        if (req.getDayEndTime() != null)    user.setDayEndTime(req.getDayEndTime());
        if (req.getMorningFocus() != null)  user.setMorningFocus(req.getMorningFocus());
        if (req.getBreakDuration() != null) user.setBreakDuration(req.getBreakDuration());
        if (req.getTaskReminders() != null) user.setTaskReminders(req.getTaskReminders());
        if (req.getAiSuggestions() != null) user.setAiSuggestions(req.getAiSuggestions());
        if (req.getMissedAlerts() != null)  user.setMissedAlerts(req.getMissedAlerts());
        if (req.getWeeklyReport() != null)  user.setWeeklyReport(req.getWeeklyReport());
        userRepository.save(user);
        return mapToProfile(user);
    }

    public void changePassword(String email, RequestDTOs.ChangePasswordRequest req) {
        User user = getCurrentUser(email);
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    public void deleteAccount(String email) {
        User user = getCurrentUser(email);
        userRepository.delete(user);
    }

    private ResponseDTOs.UserProfileResponse mapToProfile(User user) {
        ResponseDTOs.UserProfileResponse r = new ResponseDTOs.UserProfileResponse();
        r.setId(user.getId());
        r.setName(user.getName());
        r.setEmail(user.getEmail());
        r.setProfession(user.getProfession());
        r.setDayStartTime(user.getDayStartTime());
        r.setDayEndTime(user.getDayEndTime());
        r.setMorningFocus(user.isMorningFocus());
        r.setBreakDuration(user.getBreakDuration());
        r.setTaskReminders(user.isTaskReminders());
        r.setAiSuggestions(user.isAiSuggestions());
        r.setMissedAlerts(user.isMissedAlerts());
        r.setWeeklyReport(user.isWeeklyReport());
        r.setRole(user.getRole().name());
        r.setCreatedAt(user.getCreatedAt());
        return r;
    }
}