package com.kronos.service;

import com.kronos.dto.request.RequestDTOs;
import com.kronos.dto.response.ResponseDTOs;
import com.kronos.entity.DayPlan;
import com.kronos.entity.Notification;
import com.kronos.entity.Task;
import com.kronos.entity.User;
import com.kronos.exception.ResourceNotFoundException;
import com.kronos.repository.DayPlanRepository;
import com.kronos.repository.NotificationRepository;
import com.kronos.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlanService {

    @Autowired private DayPlanRepository dayPlanRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private OpenAIService openAIService;
    @Autowired private TaskService taskService;
    @Autowired private UserService userService;
    @Autowired private NotificationRepository notificationRepository;

    public ResponseDTOs.AiPlanResponse generatePlan(String email, RequestDTOs.GeneratePlanRequest request) {
        User user = userService.getCurrentUser(email);
        List<Map<String, String>> history = request.getHistory();

        if (history != null && !history.isEmpty()) {
            return openAIService.generatePlanWithHistory(
                    history,
                    user.getDayStartTime(),
                    user.getDayEndTime(),
                    user.isMorningFocus(),
                    user.getBreakDuration()
            );
        }
        return openAIService.generatePlan(
                request.getMessage(),
                user.getDayStartTime(),
                user.getDayEndTime()
        );
    }

    @Transactional
    public ResponseDTOs.PlanResponse confirmPlan(String email, RequestDTOs.ConfirmPlanRequest req) {
        User user = userService.getCurrentUser(email);
        LocalDate today = LocalDate.now();

        Optional<DayPlan> existing = dayPlanRepository.findByUserAndDate(user, today);
        if (existing.isPresent()) {
            dayPlanRepository.delete(existing.get());
            dayPlanRepository.flush();
        }

        DayPlan plan = new DayPlan();
        plan.setUser(user);
        plan.setDate(today);
        plan = dayPlanRepository.save(plan);

        final DayPlan savedPlan = plan;
        List<Task> tasks = req.getTasks().stream().map(t -> {
            Task task = new Task();
            task.setTitle(t.getTitle());
            task.setStartTime(LocalTime.parse(t.getStartTime()));
            task.setEndTime(LocalTime.parse(t.getEndTime()));
            task.setStatus(Task.Status.PENDING);
            task.setPlan(savedPlan);
            return task;
        }).collect(Collectors.toList());

        taskRepository.saveAll(tasks);
        taskService.updateTaskStatuses(tasks);

        createPlanNotification(user,
                "Your plan for today is ready! " + tasks.size() + " tasks scheduled. Let's have a productive day!");

        return mapToPlanResponse(plan, tasks);
    }

    public ResponseDTOs.PlanResponse getTodayPlan(String email) {
        User user = userService.getCurrentUser(email);
        Optional<DayPlan> plan = dayPlanRepository.findByUserAndDate(user, LocalDate.now());
        if (plan.isEmpty()) return null;
        List<Task> tasks = taskRepository.findByPlan(plan.get());
        taskService.updateTaskStatuses(tasks);
        return mapToPlanResponse(plan.get(), tasks);
    }

    public ResponseDTOs.PlanResponse getPlanByDate(String email, LocalDate date) {
        User user = userService.getCurrentUser(email);
        DayPlan plan = dayPlanRepository.findByUserAndDate(user, date)
                .orElseThrow(() -> new ResourceNotFoundException("No plan found for date: " + date));
        List<Task> tasks = taskRepository.findByPlan(plan);
        taskService.updateTaskStatuses(tasks);
        return mapToPlanResponse(plan, tasks);
    }

    @Transactional
    public ResponseDTOs.AiPlanResponse regeneratePlan(String email, RequestDTOs.GeneratePlanRequest request) {
        User user = userService.getCurrentUser(email);
        Optional<DayPlan> existing = dayPlanRepository.findByUserAndDate(user, LocalDate.now());
        if (existing.isPresent()) {
            dayPlanRepository.delete(existing.get());
            dayPlanRepository.flush();
        }
        return generatePlan(email, request);
    }

    @Transactional
    public void deleteTodayPlan(String email) {
        User user = userService.getCurrentUser(email);
        dayPlanRepository.findByUserAndDate(user, LocalDate.now())
                .ifPresent(plan -> {
                    dayPlanRepository.delete(plan);
                    dayPlanRepository.flush();
                });
    }

    private void createPlanNotification(User user, String message) {
        try {
            Notification n = new Notification();
            n.setUser(user);
            n.setMessage(message);
            n.setType(Notification.Type.AI_SUGGESTION);
            notificationRepository.save(n);
        } catch (Exception e) {
            System.err.println("[Notification] Failed: " + e.getMessage());
        }
    }

    private ResponseDTOs.PlanResponse mapToPlanResponse(DayPlan plan, List<Task> tasks) {
        List<ResponseDTOs.TaskResponse> taskResponses = tasks.stream()
                .map(taskService::mapToResponse)
                .collect(Collectors.toList());
        return new ResponseDTOs.PlanResponse(plan.getId(), plan.getDate(), taskResponses);
    }

}