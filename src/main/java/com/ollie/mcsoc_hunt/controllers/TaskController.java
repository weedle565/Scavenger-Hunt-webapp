package com.ollie.mcsoc_hunt.controllers;

import com.ollie.mcsoc_hunt.entities.Task;

import com.ollie.mcsoc_hunt.helpers.JwtGenerator;
import com.ollie.mcsoc_hunt.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

@RestController
// [url]/api/task
@RequestMapping("/api/task")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Task>> getAllTasks(@RequestHeader("Authorization") String auth) {
        if (!JwtGenerator.checkJWT(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<Task> response = taskService.getAllTasks();

        if (response == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all/admin")
    public ResponseEntity<List<Task>> getAllTasksAdmin(@RequestHeader("Authorization") String auth) {
        if (!JwtGenerator.checkAdminJWT(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task, @RequestHeader("Authorization") String auth) {

        if (!JwtGenerator.checkAdminJWT(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        URI location = null;
        Task createdTask = null;

        try {

            createdTask = taskService.createTask(task);
            location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(createdTask.getId())
                    .toUri();
        } catch (Exception e) {
            Logger.getLogger("Task").warning("Failed to create task");
        }

        if (location == null) return ResponseEntity.badRequest().build();

        return ResponseEntity.created(location).body(createdTask);

    }

    @GetMapping("/id/{taskId}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long taskId, @RequestHeader("Authorization") String auth) {

        if (!JwtGenerator.checkJWT(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Task task = taskService.getTaskById(taskId, auth);

        return ResponseEntity.ok(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task taskDetails, @RequestHeader("Authorization") String auth)  {

        if (!JwtGenerator.checkAdminJWT(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Task updatedTask = taskService.updateTask(id, taskDetails);

        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, @RequestHeader("Authorization") String auth) {

        if (!JwtGenerator.checkAdminJWT(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

}
