package com.kronos.service;

import com.kronos.dto.request.RequestDTOs;
import com.kronos.dto.response.ResponseDTOs;
import com.kronos.entity.DayPlan;
import com.kronos.entity.Notification;
import com.kronos.entity.Task;
import com.kronos.entity.User;
import com.kronos.exception.ResourceNotFoundException;
import com.kronos.exception.ValidationException;
import com.kronos.repository.DayPlanRepository;
import com.kronos.repository.NotificationRepository;
import com.kronos.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class TaskService {

    @Autowired private TaskRepository taskRepository;
    @Autowired private DayPlanRepository dayPlanRepository;
    @Autowired private NotificationRepository notificationRepository;

    public void updateTaskStatuses(List<Task> tasks) {
        LocalTime now = LocalTime.now();
        for (Task task : tasks) {
            if (task.getStatus() == Task.Status.COMPLETED) continue;

            Task.Status oldStatus = task.getStatus();

            if (now.isBefore(task.getStartTime())) {
                task.setStatus(Task.Status.PENDING);
            } else if (!now.isBefore(task.getStartTime()) && now.isBefore(task.getEndTime())) {
                task.setStatus(Task.Status.IN_PROGRESS);
            } else if (!now.isBefore(task.getEndTime())) {
                task.setStatus(Task.Status.MISSED);
            }

            if (oldStatus != Task.Status.MISSED && task.getStatus() == Task.Status.MISSED) {
                createNotification(
                        task.getPlan().getUser(),
                        "You missed: " + task.getTitle() + " (" + task.getStartTime() + " - " + task.getEndTime() + ")",
                        Notification.Type.WARNING
                );
            }

            if (oldStatus != Task.Status.IN_PROGRESS && task.getStatus() == Task.Status.IN_PROGRESS) {
                createNotification(
                        task.getPlan().getUser(),
                        "Time to start: " + task.getTitle() + " — ends at " + task.getEndTime(),
                        Notification.Type.TASK_REMINDER
                );
            }

            taskRepository.save(task);
        }
    }

    public ResponseDTOs.TaskResponse completeTask(Long taskId, String userEmail) {
        Task task = getTaskForUser(taskId, userEmail);
        if (task.getStatus() == Task.Status.COMPLETED) {
            throw new ValidationException("Task is already completed");
        }
        task.setStatus(Task.Status.COMPLETED);
        taskRepository.save(task);

        createNotification(
                task.getPlan().getUser(),
                "Great job! You completed: " + task.getTitle() + " ✓",
                Notification.Type.SUCCESS
        );

        return mapToResponse(task);
    }

    public void deleteTask(Long taskId, String userEmail) {
        Task task = getTaskForUser(taskId, userEmail);
        if (task.getStatus() != Task.Status.PENDING) {
            throw new ValidationException("Only PENDING tasks can be deleted");
        }
        taskRepository.delete(task);
    }

    public ResponseDTOs.TaskResponse updateTask(Long taskId, String userEmail, RequestDTOs.UpdateTaskRequest req) {
        Task task = getTaskForUser(taskId, userEmail);
        if (task.getStatus() != Task.Status.PENDING) {
            throw new ValidationException("Only PENDING tasks can be modified");
        }
        if (req.getTitle() != null) task.setTitle(req.getTitle());
        if (req.getStartTime() != null) task.setStartTime(LocalTime.parse(req.getStartTime()));
        if (req.getEndTime() != null) task.setEndTime(LocalTime.parse(req.getEndTime()));
        taskRepository.save(task);
        return mapToResponse(task);
    }

    private Task getTaskForUser(Long taskId, String userEmail) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        DayPlan plan = task.getPlan();
        if (!plan.getUser().getEmail().equals(userEmail)) {
            throw new ValidationException("Access denied");
        }
        return task;
    }

    private void createNotification(User user, String message, Notification.Type type) {
        try {
            Notification n = new Notification();
            n.setUser(user);
            n.setMessage(message);
            n.setType(type);
            notificationRepository.save(n);
        } catch (Exception e) {
            System.err.println("[Notification] Failed to create: " + e.getMessage());
        }
    }

    public ResponseDTOs.TaskResponse mapToResponse(Task task) {
        return new ResponseDTOs.TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getStartTime().toString(),
                task.getEndTime().toString(),
                task.getStatus()
        );
    }
}