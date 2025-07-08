package com.ollie.mcsoc_hunt.controllers;

import com.ollie.mcsoc_hunt.entities.Task;
import com.ollie.mcsoc_hunt.helpers.GuessResults;
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
@RequestMapping("/api/task")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Task>> getAllTasks(@RequestHeader("Authorization") String auth) {
        System.out.println("Auth: " + auth);
        if (!JwtGenerator.checkJWT(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<Task> response = taskService.getAllTasks(auth);

        if (response == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(response);
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

        System.out.println(task);

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

    @GetMapping("/id/{taskId}/{guess}")
    public ResponseEntity<GuessResults> checkGuess(
            @PathVariable Long taskId,
            @PathVariable String guess,
            @RequestHeader("Authorization") String authHeader) {

        // Extract token from header
        String token = authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader;

        // Validate the token
        if (!JwtGenerator.checkJWT("Bearer " + token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Call the service with the extracted token
        GuessResults results = taskService.checkCorrectGuess(guess, taskId, token);

        return ResponseEntity.ok(results);
    }

//    @PostMapping("team/{teamId}/completed/{id}")
//    public ResponseEntity<Void> markTaskCompleted(@PathVariable Long id, @PathVariable Long teamId) {
//        taskService.markTaskCompleted(id);
//        return ResponseEntity.ok().build();
//    }

    @PostMapping("/{taskId}/subtask")
    public ResponseEntity<Void> startSubtask(
            @PathVariable("taskId") Long taskId,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            @RequestHeader("Authorization") String auth
    ) {
        if (!JwtGenerator.checkJWT(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        taskService.startSubtask(taskId, auth, startTime, endTime);

        return ResponseEntity.noContent().build();
    }

}
