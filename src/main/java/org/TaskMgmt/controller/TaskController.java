package org.TaskMgmt.controller;

import org.TaskMgmt.model.AuthenticateUser;
import org.TaskMgmt.model.Task;
import org.TaskMgmt.model.TaskSpecification;
import org.TaskMgmt.model.User;
import org.TaskMgmt.repository.TaskRepository;
import org.TaskMgmt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AuthenticateUser authenticateUser;

    @GetMapping("/getTasks")
    public Page<Task> getTasks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadlineStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadlineEnd,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "task_id") String sort, @RequestHeader String authToken) throws Exception {
        User user = authenticateUser.authenticateUser(authToken);
        if(user==null)
        {
            throw new Exception("Invalid credentials, please enter valid credentials");
        }

        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Page index must be non-negative and size must be greater than 0");
        }

        if (deadlineStart != null && deadlineEnd != null && deadlineStart.isAfter(deadlineEnd)) {
            throw new IllegalArgumentException("deadlineStart cannot be after deadlineEnd");
        }

        Specification<Task> spec = TaskSpecification.filterTasks(title, status, deadlineStart, deadlineEnd, priority, category);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return taskRepository.findAll(spec, pageable);
    }

    @PostMapping("/createTask")
    public ResponseEntity<Task> createTask(@RequestBody Task task, @RequestHeader String authToken) throws Exception {
        User user = authenticateUser.authenticateUser(authToken);
        if(user==null)
        {
            throw new Exception("Invalid credentials, please enter valid credentials");
        }
        Task savedTask = taskRepository.save(task);
        return ResponseEntity.ok(savedTask);
    }

    @GetMapping("/user/{userId}")
    public List<Task> getTasksByUser(@PathVariable Long userId, @RequestHeader String authToken) throws Exception {

        User user = authenticateUser.authenticateUser(authToken);
        if(user==null)
        {
            throw new Exception("Invalid credentials, please enter valid credentials");
        }

        return taskRepository.findByUserUserId(userId);
    }

}

