package com.kronos.controller;

import com.kronos.dto.request.RequestDTOs;
import com.kronos.dto.response.ResponseDTOs;
import com.kronos.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired private TaskService taskService;

    @PutMapping("/{id}/complete")
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.TaskResponse>> complete(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Task completed",
                taskService.completeTask(id, auth.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTOs.ApiResponse<Void>> delete(
            @PathVariable Long id, Authentication auth) {
        taskService.deleteTask(id, auth.getName());
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Task deleted", null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.TaskResponse>> update(
            @PathVariable Long id, Authentication auth,
            @RequestBody RequestDTOs.UpdateTaskRequest request) {
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.ok("Task updated",
                taskService.updateTask(id, auth.getName(), request)));
    }
}
