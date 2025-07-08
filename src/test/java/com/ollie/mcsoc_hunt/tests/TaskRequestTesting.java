package com.ollie.mcsoc_hunt.tests;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollie.mcsoc_hunt.entities.Task;
import com.ollie.mcsoc_hunt.entities.Team;
import com.ollie.mcsoc_hunt.exceptions.TaskNotFoundException;
import com.ollie.mcsoc_hunt.helpers.GuessResults;
import com.ollie.mcsoc_hunt.services.TaskService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        task.setName("test team");
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

        mockMvc.perform(post("/api/task").
                header("Authorization", "Bearer " + generateAdminTestJWT()).
                contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(taskToCreate))).
                andExpect(status().isCreated()).
                andExpect(jsonPath("$.id").value(1L)).
                andExpect(jsonPath("$.name").value("test team"));

    }

    @Test
    void getTaskCorrectTest() throws Exception {

        Team team = new Team();
        team.setId(1L);
        team.setName("test team");
        ArrayList<String> list = new ArrayList<>();
        list.add("Test location");
        team.setRevealedLocations(list);

        String jwt = generateFakeTestJWT();
        String bearerJwt = "Bearer " + jwt;

        when(taskService.getTaskById(Mockito.eq(1L), Mockito.eq(bearerJwt))).thenReturn(task);

        mockMvc.perform(get("/api/task/id/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("test team"))
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

        String jwt = generateFakeTestJWT();
        String bearerJwt = "Bearer " + jwt;

        when(taskService.getTaskById(Mockito.eq(1L), Mockito.eq(bearerJwt))).thenReturn(task);

        mockMvc.perform(get("/api/task/id/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearerJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Secret Location"));
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

        String jwt = generateFakeTestJWT();
        String bearerJwt = "Bearer " + jwt;

        when(taskService.getTaskById(Mockito.eq(1L), Mockito.eq(bearerJwt))).thenReturn(task);

        mockMvc.perform(get("/api/task/id/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + generateFakeTestJWT()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Secret Location"));

    }

    @Test
    void getTaskNotFound() throws Exception {
        Long taskId = 99L;

        Team team = new Team();
        team.setId(1L);
        team.setName("test team");
        team.setRevealedLocations(new ArrayList<>());

        String jwt = generateFakeTestJWT();
        String bearerJwt = "Bearer " + jwt;

        // Mock the service to throw exception on delete
        Mockito.doThrow(new TaskNotFoundException("Task not found"))
                .when(taskService)
                .getTaskById(taskId, bearerJwt);

        mockMvc.perform(get("/api/task/id/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearerJwt)).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.message").value("Task not found"));
    }

    @Test
    void getTaskInternalServerError() throws Exception {
        Long taskId = 4L;

        Mockito.doThrow(new RuntimeException("Database error")).when(taskService).getTaskById(taskId, generateFakeTestJWT());

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

        mockMvc.perform(get("/api/task/id/{id}", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delTasksTest() throws Exception {
        Long taskId = 3L;

        Mockito.doNothing().when(taskService).deleteTask(taskId);

        mockMvc.perform(delete("/api/task/{id}", taskId)
                .header("Authorization", "Bearer " + generateAdminTestJWT())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTaskNotFound() throws Exception {
        Long taskId = 99L;

        // Mock the service to throw exception on delete
        Mockito.doThrow(new TaskNotFoundException("Task not found")).when(taskService).deleteTask(taskId);

        mockMvc.perform(delete("/api/task/{id}", taskId)
                        .header("Authorization", "Bearer " + generateAdminTestJWT())
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
        // Arrange
        String jwt = generateFakeTestJWT();  // generate JWT once and reuse
        String bearerJwt = "Bearer " + jwt;

        Team team = new Team();
        team.setId(1L);
        team.setName("Team A");
        team.setRevealedLocations(new ArrayList<>());

        String guess = "Test location";

        GuessResults checkResults = new GuessResults();
        checkResults.setCorrect(true);

        // Use eq() matcher to strictly match arguments including the stripped token
        when(taskService.checkCorrectGuess(Mockito.eq(guess), Mockito.eq(1L), Mockito.eq(jwt))).thenReturn(checkResults);

        // Act & Assert
        mockMvc.perform(get("/api/task/id/1/{guess}", guess)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearerJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true));
    }

    @Test
    void getAllTasksTest() throws Exception {
        List<Task> tasks = List.of(task);
        when(taskService.getAllTasks(Mockito.anyString())).thenReturn(tasks);

        mockMvc.perform(get("/api/task/all")
                        .header("Authorization", "Bearer " + generateFakeTestJWT()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("test team"));
    }

    @Test
    void getAllTasksUnauthorizedTest() throws Exception {
        mockMvc.perform(get("/api/task/all").header("Authorization", "Bearer loll"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTaskTest() throws Exception {
        Task updated = new Task();
        updated.setId(1L);
        updated.setName("Updated task");

        when(taskService.updateTask(Mockito.eq(1L), Mockito.any(Task.class))).thenReturn(updated);

        mockMvc.perform(put("/api/task/1")
                        .header("Authorization", "Bearer " + generateAdminTestJWT())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated task"));
    }

    @Test
    void updateTaskUnauthorizedTest() throws Exception {
        mockMvc.perform(put("/api/task/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer loll")
                        .content(objectMapper.writeValueAsString(task))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void startSubtaskTest() throws Exception {
        Mockito.doNothing().when(taskService).startSubtask(Mockito.eq(1L), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        mockMvc.perform(post("/api/task/1/subtask")
                        .param("startTime", "10:00")
                        .param("endTime", "11:00")
                        .header("Authorization", "Bearer " + generateFakeTestJWT()))
                .andExpect(status().isNoContent());
    }

    @Test
    void startSubtaskUnauthorizedTest() throws Exception {
        mockMvc.perform(post("/api/task/1/subtask")
                        .param("startTime", "10:00")
                        .param("endTime", "11:00")
                        .header("Authorization", "Bearer loll"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCorrectGuessUnauthorized() throws Exception {
        mockMvc.perform(get("/api/task/id/1/Test location")
                        .header("Authorization", "Bearer lol" ))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTaskUnauthorizedTest() throws Exception {
        Task taskToCreate = new Task();
        taskToCreate.setName("Unauthorized");

        mockMvc.perform(post("/api/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer lol" )
                        .content(objectMapper.writeValueAsString(taskToCreate)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTaskNotFoundTest() throws Exception {
        Mockito.doThrow(new TaskNotFoundException("Task not found"))
                .when(taskService)
                .updateTask(Mockito.eq(999L), Mockito.any(Task.class));

        mockMvc.perform(put("/api/task/999")
                        .header("Authorization", "Bearer " + generateAdminTestJWT())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found"));
    }

    private String generateAdminTestJWT() {
        long expirationMs = 86400000;
        return Jwts.builder()
                .setSubject("test team")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs)).signWith(SignatureAlgorithm.HS256, com.ollie.mcsoc_hunt.helpers.JwtGenerator.getAdminSecret().getBytes(StandardCharsets.UTF_8)).compact();
    }

    private String generateFakeTestJWT() {
        long expirationMs = 86400000;
        return Jwts.builder()
                .setSubject("notreal")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs)).signWith(SignatureAlgorithm.HS256, com.ollie.mcsoc_hunt.helpers.JwtGenerator.getSecret().getBytes(StandardCharsets.UTF_8)).compact();
    }


}
