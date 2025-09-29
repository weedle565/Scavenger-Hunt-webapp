package com.ollie.mcsoc_hunt.services;

import com.ollie.mcsoc_hunt.entities.Task;
import com.ollie.mcsoc_hunt.entities.Team;
import com.ollie.mcsoc_hunt.exceptions.TaskNotFoundException;
import com.ollie.mcsoc_hunt.exceptions.TeamNotFoundException;
import com.ollie.mcsoc_hunt.helpers.GuessResults;
import com.ollie.mcsoc_hunt.helpers.JwtGenerator;
import com.ollie.mcsoc_hunt.repositories.TaskRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/*

Requirements:

    Add task
    Get all tasks
    Get specific task
    Update task
    Get subtask (if completed)

 */

@Service
public class TaskService {

    private final TaskRepo taskRepo;
    private final TeamService teamService;

    TaskService(TaskRepo taskRepo, TeamService teamService) {
        this.taskRepo = taskRepo;
        this.teamService = teamService;
    }

    public List<Task> getAllTasks() {

        return taskRepo.findAll();
    }

    public Task createTask(Task task) {
        return taskRepo.save(task);
    }

    public Task getTaskById(Long taskId, String cookie) {

        Team authTeam = teamService.getTeamByCookie(cookie);

        if (authTeam == null) throw new TeamNotFoundException("Team not found");

        return taskRepo.findById(taskId).orElseThrow(() -> new TaskNotFoundException("Could not find task: " + taskId));

    }

    public Task updateTask(Long id, Task taskDetails) {
        Task task = taskRepo.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        task.setName(taskDetails.getName());
        return taskRepo.save(task);
    }

    public void deleteTask(Long id) {
        boolean exists = taskRepo.existsById(id);
        if (!exists) throw new TaskNotFoundException("Team not found: " + id);

        taskRepo.deleteById(id);
    }

}
