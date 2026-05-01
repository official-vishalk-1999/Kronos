package com.kronos.service;

import com.kronos.dto.response.ResponseDTOs;
import com.kronos.entity.DayPlan;
import com.kronos.entity.Task;
import com.kronos.entity.User;
import com.kronos.repository.DayPlanRepository;
import com.kronos.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DashboardService {

    @Autowired private DayPlanRepository dayPlanRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private UserService userService;
    @Autowired private TaskService taskService;

    public ResponseDTOs.DashboardStats getTodayStats(String email) {
        User user = userService.getCurrentUser(email);
        Optional<DayPlan> planOpt = dayPlanRepository.findByUserAndDate(user, LocalDate.now());

        if (planOpt.isEmpty()) {
            return new ResponseDTOs.DashboardStats(0, 0, 0, 0, 0, 0.0);
        }

        List<Task> tasks = taskRepository.findByPlan(planOpt.get());
        taskService.updateTaskStatuses(tasks);

        int total = tasks.size();
        int completed = (int) tasks.stream().filter(t -> t.getStatus() == Task.Status.COMPLETED).count();
        int missed = (int) tasks.stream().filter(t -> t.getStatus() == Task.Status.MISSED).count();
        int pending = (int) tasks.stream().filter(t -> t.getStatus() == Task.Status.PENDING).count();
        int inProgress = (int) tasks.stream().filter(t -> t.getStatus() == Task.Status.IN_PROGRESS).count();

        double productivity = total > 0 ? (double) completed / total * 100 : 0;

        return new ResponseDTOs.DashboardStats(total, completed, missed, pending, inProgress,
                Math.round(productivity * 10.0) / 10.0);
    }

    public ResponseDTOs.WeeklyStats getWeeklyStats(String email) {
        User user = userService.getCurrentUser(email);
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE");

        List<String> labels = new ArrayList<>();
        List<Double> productivity = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            labels.add(date.format(formatter));

            Optional<DayPlan> plan = dayPlanRepository.findByUserAndDate(user, date);
            if (plan.isPresent()) {
                List<Task> tasks = taskRepository.findByPlan(plan.get());
                int total = tasks.size();
                if (total > 0) {
                    long completed = tasks.stream().filter(t -> t.getStatus() == Task.Status.COMPLETED).count();
                    productivity.add(Math.round((double) completed / total * 1000.0) / 10.0);
                } else {
                    productivity.add(0.0);
                }
            } else {
                productivity.add(0.0);
            }
        }

        return new ResponseDTOs.WeeklyStats(labels, productivity);
    }
}
