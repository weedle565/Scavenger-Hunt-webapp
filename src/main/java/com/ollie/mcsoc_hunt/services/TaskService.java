package com.ollie.mcsoc_hunt.services;

import com.ollie.mcsoc_hunt.entities.Task;
import com.ollie.mcsoc_hunt.entities.Team;
import com.ollie.mcsoc_hunt.exceptions.TaskNotFoundException;
import com.ollie.mcsoc_hunt.helpers.GuessResults;
import com.ollie.mcsoc_hunt.repositories.TaskRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public List<Task> getAllTasks(Long teamId) {

        List<Task> censoredTasks = new ArrayList<>();
        Team team = teamService.getTeamById(teamId);

        for (Task checkingTask : taskRepo.findAll()) {
            censoredTasks.add(getTaskWithLocationCensored(checkingTask, team));
        }

        return censoredTasks;
    }

    public Task createTask(Task task) {
        return taskRepo.save(task);
    }

    public Task getTaskById(Long taskId, Long teamId) {
        Task censoredTask = taskRepo.findById(taskId).orElseThrow(() -> new TaskNotFoundException("Could not find task: " + taskId));

        Team checkedTeam = teamService.getTeamById(teamId);
        return getTaskWithLocationCensored(censoredTask, checkedTeam);
    }

    private Task getTaskWithLocationCensored(Task task, Team team) {
        Task newTask = new Task();

        newTask.setRiddle(task.getRiddle());
        newTask.setDescription(task.getDescription());
        newTask.setName(task.getName());
        newTask.setId(task.getId());
        newTask.setHasSubtask(task.getHasSubtask());
        newTask.setSubtaskComplete(task.getSubtaskComplete());

        if (team.getRevealedLocations().contains(task.getLocation())) {
            newTask.setLocation(task.getLocation());
            newTask.setSubtaskDescription(task.getSubtaskDescription());
            newTask.setCompleted(true);
        } else {

            newTask.setSubtaskDescription("Hidden");
            newTask.setLocation("Hidden");
            newTask.setCompleted(false);
        }

        return newTask;
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

    public GuessResults checkCorrectGuess(String guess, Long taskId, Long teamId) {
        Task task = taskRepo.findById(taskId).orElseThrow(() -> new TaskNotFoundException("Could not find task: " + taskId));

        Team team = teamService.getTeamById(teamId);

        if (task.getLocation().equalsIgnoreCase(guess) ) {
            teamService.addGuess(guess, team);
            return new GuessResults(true, "Correct!");
        }

        return new GuessResults(false, "Incorrect!");
    }

    public void startSubtask(Long taskId, Long teamId, String startTime, String endTime) {

        Team team = teamService.getTeamById(teamId);
        Task task = taskRepo.findById(taskId).orElseThrow(() -> new TaskNotFoundException("Task not found"));

        Task edittedTask = new Task();

        if (!edittedTask.getHasSubtask()) return;

        edittedTask.setSubtaskTimeStarted(startTime);
        edittedTask.setId(task.getId());
        edittedTask.setDescription(edittedTask.getDescription());
        edittedTask.setSubtaskTimeEnding(endTime);
        edittedTask.setSubtaskDescription(task.getSubtaskDescription());

        taskRepo.deleteById(taskId);
        taskRepo.save(edittedTask);

        teamService.getTeamRepo().save(team);

    }

}
