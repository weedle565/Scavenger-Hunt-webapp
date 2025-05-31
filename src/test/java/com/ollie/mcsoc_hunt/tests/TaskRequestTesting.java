package com.ollie.mcsoc_hunt.tests;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollie.mcsoc_hunt.entities.Task;
import com.ollie.mcsoc_hunt.entities.Team;
import com.ollie.mcsoc_hunt.exceptions.TaskNotFoundException;
import com.ollie.mcsoc_hunt.exceptions.TeamNotFoundException;
import com.ollie.mcsoc_hunt.helpers.GuessResults;
import com.ollie.mcsoc_hunt.services.TaskService;
import com.ollie.mcsoc_hunt.services.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskRequestTesting {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private Task task;

    @BeforeEach
    void setupTeam() {
        task = new Task();
        task.setId(1L);
        task.setName("test task");
        task.setDescription("Test task");
        task.setCompleted(false);
        task.setRiddle("Test riddle");
        task.setLocation("Test location");

    }

    @Test
    void createTaskTest() throws Exception {

        Task taskToCreate = new Task();
        taskToCreate.setName("test task");


        when(taskService.createTask(Mockito.any(Task.class))).thenReturn(task);

        mockMvc.perform(post("/api/task").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(taskToCreate))).
                andExpect(status().isCreated()).
                andExpect(jsonPath("$.id").value(1L)).
                andExpect(jsonPath("$.name").value("test task"));

    }

    @Test
    void getTasksTest() throws Exception {

        List<Task> tasks = List.of(task);

        Team team = new Team();
        team.setId(1L);
        team.setName("test team");
        team.setRevealedLocations(new ArrayList<>());

        when(taskService.getAllTasks(team.getId())).thenReturn(tasks);

        mockMvc.perform(get("/api/task/team/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("test task"));
    }

    @Test
    void getTaskCorrectTest() throws Exception {

        Team team = new Team();
        team.setId(1L);
        team.setName("test team");
        ArrayList<String> list = new ArrayList<>();
        list.add("Test location");
        team.setRevealedLocations(list);

        when(taskService.getTaskById(Mockito.eq(1L), Mockito.eq(1L))).thenReturn(task);

        mockMvc.perform(get("/api/task/1/team/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("test task"))
                .andExpect(jsonPath("$.location").value("Test location"));

    }

    @Test
    void getTaskWrongLocationsTest() throws Exception {

        Task task = new Task();
        task.setId(1L);
        task.setName("Test Task");
        task.setDescription("Test Desc");
        task.setLocation("Secret Location");
        task.setCompleted(false);
        task.setRiddle("Riddle");

        Team team = new Team();
        team.setId(1L);
        team.setName("Team A");
        team.setRevealedLocations(List.of("Another Location"));

        Task censoredTask = new Task();
        censoredTask.setId(task.getId());
        censoredTask.setName(task.getName());
        censoredTask.setDescription(task.getDescription());
        censoredTask.setCompleted(task.getCompleted());
        censoredTask.setRiddle(task.getRiddle());
        censoredTask.setLocation("Hidden");

        when(taskService.getTaskById(1L, 1L)).thenReturn(censoredTask);

        mockMvc.perform(get("/api/task/1/team/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Hidden"));
    }

    @Test
    void getTaskNoLocationsTest() throws Exception {

        Task task = new Task();
        task.setId(1L);
        task.setName("Test Task");
        task.setDescription("Test Desc");
        task.setLocation("Secret Location");
        task.setCompleted(false);
        task.setRiddle("Riddle");

        Team team = new Team();
        team.setId(1L);
        team.setName("Team A");
        team.setRevealedLocations(new ArrayList<>());

        Task censoredTask = new Task();
        censoredTask.setId(task.getId());
        censoredTask.setName(task.getName());
        censoredTask.setDescription(task.getDescription());
        censoredTask.setCompleted(task.getCompleted());
        censoredTask.setRiddle(task.getRiddle());
        censoredTask.setLocation("Hidden");

        when(taskService.getTaskById(1L, 1L)).thenReturn(censoredTask);

        mockMvc.perform(get("/api/task/1/team/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Hidden"));

    }

    @Test
    void getTaskNotFound() throws Exception {
        Long taskId = 99L;

        Team team = new Team();
        team.setId(1L);
        team.setName("test team");
        team.setRevealedLocations(new ArrayList<>());

        // Mock the service to throw exception on delete
        Mockito.doThrow(new TaskNotFoundException("Task not found")).when(taskService).getTaskById(taskId, team.getId());

        mockMvc.perform(get("/api/task/{id}/team/1", taskId)
                        .contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.message").value("Task not found"));
    }

    @Test
    void getTaskInternalServerError() throws Exception {
        Long taskId = 4L;
        Long teamId = 32L;

        Mockito.doThrow(new RuntimeException("Database error")).when(taskService).getTaskById(taskId, teamId);

        mockMvc.perform(get("/api/task/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getTaskInvalidIdFormat() throws Exception {
        Team team = new Team();
        team.setId(1L);
        team.setName("test team");
        team.setRevealedLocations(new ArrayList<>());

        mockMvc.perform(get("/api/task/{id}/team/1", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delTasksTest() throws Exception {
        Long taskId = 3L;

        Mockito.doNothing().when(taskService).deleteTask(taskId);

        mockMvc.perform(delete("/api/task/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTaskNotFound() throws Exception {
        Long taskId = 99L;

        // Mock the service to throw exception on delete
        Mockito.doThrow(new TaskNotFoundException("Task not found")).when(taskService).deleteTask(taskId);

        mockMvc.perform(delete("/api/task/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).
                andExpect(jsonPath("$.message").value("Task not found"));
    }

    @Test
    void deleteTaskInternalServerError() throws Exception {
        Long taskId = 4L;

        Mockito.doThrow(new RuntimeException("Database error")).when(taskService).deleteTask(taskId);

        mockMvc.perform(delete("/api/task/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteTaskInvalidIdFormat() throws Exception {
        mockMvc.perform(delete("/api/task/{id}", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCorrectGuess() throws Exception {

        Team team = new Team();
        team.setId(1L);
        team.setName("Team A");
        team.setRevealedLocations(new ArrayList<>());

        String guess = "test";

        GuessResults checkResults = new GuessResults();

        when(taskService.checkCorrectGuess(guess, 1L, 1L)).thenReturn(checkResults);

        mockMvc.perform(get("/api/task/1/team/1/guess/{guess}", guess)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Hidden"));
    }
//
//    @Test
//    void testIncorrectGuess() {
//        Long taskId = 1L;
//        Long teamId = 10L;
//        String guess = "mouse";
//
//        Task task = new Task();
//        task.setId(taskId);
//        task.setLocation("keyboard");
//
//        Team team = new Team();
//        team.setId(teamId);
//
//        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));
//        when(teamService.getTeamById(teamId)).thenReturn(team);
//
//        GuessResults result = taskService.checkCorrectGuess(guess, taskId, teamId);
//
//        assertFalse(result.correct());
//        assertEquals("Incorrect!", result.message());
//        verify(teamService, never()).addGuess(anyString(), any());
//    }
//
//    @Test
//    void testTaskNotFound() {
//        Long taskId = 99L;
//        Long teamId = 10L;
//
//        when(taskRepo.findById(taskId)).thenReturn(Optional.empty());
//
//        assertThrows(TaskNotFoundException.class, () ->
//                taskService.checkCorrectGuess("keyboard", taskId, teamId));
//    }

}
