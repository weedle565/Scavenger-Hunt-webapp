package com.ollie.mcsoc_hunt.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollie.mcsoc_hunt.exceptions.TeamNotFoundException;
import com.ollie.mcsoc_hunt.entities.Team;
import com.ollie.mcsoc_hunt.helpers.GuessResults;
import com.ollie.mcsoc_hunt.services.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TeamRequestTesting {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamService teamService;

    private Team team;

    @BeforeEach
    void setupTeam() {
        team = new Team();
        team.setId(1L);
        team.setName("test team");
    }

    @Test
    void createTeamTest() throws Exception {

        Team teamToCreate = new Team();
        teamToCreate.setName("test team");


        Mockito.when(teamService.createTeam(Mockito.any(Team.class))).thenReturn(team);

        mockMvc.perform(post("/api/team").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(teamToCreate))).
                andExpect(status().isCreated()).
                andExpect(jsonPath("$.id").value(1L)).
                andExpect(jsonPath("$.name").value("test team"));

    }

    @Test
    void getTeamsTest() throws Exception {

        List<Team> teams = List.of(team);

        Mockito.when(teamService.getAllTeams()).thenReturn(teams);

        mockMvc.perform(get("/api/team")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("test team"));
    }

    @Test
    void getTeamTest() throws Exception {

        Mockito.when(teamService.getTeamById(Mockito.eq(1L))).thenReturn(team);

        mockMvc.perform(get("/api/team/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("test team"));
    }

    @Test
    void getTeamNotFound() throws Exception {
        Long teamId = 99L;

        // Mock the service to throw exception on delete
        Mockito.doThrow(new TeamNotFoundException("Team not found")).when(teamService).getTeamById(teamId);

        mockMvc.perform(get("/api/team/{id}", teamId)
                        .contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.message").value("Team not found"));
    }

    @Test
    void getTeamInternalServerError() throws Exception {
        Long teamId = 4L;

        Mockito.doThrow(new RuntimeException("Database error")).when(teamService).getTeamById(teamId);

        mockMvc.perform(get("/api/team/{id}", teamId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getTeamInvalidIdFormat() throws Exception {
        mockMvc.perform(get("/api/team/{id}", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delTeamsTest() throws Exception {
        Long teamId = 3L;

        Mockito.doNothing().when(teamService).deleteTeam(teamId);

        mockMvc.perform(delete("/api/team/{id}", teamId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTeamNotFound() throws Exception {
        Long teamId = 99L;

        // Mock the service to throw exception on delete
        Mockito.doThrow(new TeamNotFoundException("Team not found")).when(teamService).deleteTeam(teamId);

        mockMvc.perform(delete("/api/team/{id}", teamId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).
                andExpect(jsonPath("$.message").value("Team not found"));
    }

    @Test
    void deleteTeamInternalServerError() throws Exception {
        Long teamId = 4L;

        Mockito.doThrow(new RuntimeException("Database error")).when(teamService).deleteTeam(teamId);

        mockMvc.perform(delete("/api/team/{id}", teamId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteTeamInvalidIdFormat() throws Exception {
        mockMvc.perform(delete("/api/team/{id}", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

//    @Test
//    void teamGuessSuccess() throws Exception {
//        String location = "test";
//
//        GuessResults mockResult = GuessResults.builder()
//                .correct(true)
//                .message("Correct Location")
//                .build();
//
//        Mockito.when(teamService.checkGuess(1L, location)).thenReturn(mockResult);
//
//        mockMvc.perform(post("/api/team/1/guess/{location}", location)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.correct").value(true))
//                .andExpect(jsonPath("$.message").value("Correct Location"));
//    }
//
//    @Test
//    void teamIncorrectGuessSuccess() throws Exception {
//        String location = "test3532";
//
//        GuessResults mockResult = GuessResults.builder()
//                .correct(false)
//                .message("Incorrect location")
//                .build();
//
//        Mockito.when(teamService.checkGuess(1L, location)).thenReturn(mockResult);
//
//        mockMvc.perform(post("/api/team/1/guess/{location}", location)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.correct").value(false))
//                .andExpect(jsonPath("$.message").value("Incorrect location"));
//    }
//
//    @Test
//    void teamEmptyGuess() throws Exception {
//        String location = "";
//
//        GuessResults mockResult = GuessResults.builder()
//                .correct(false)
//                .message("Incorrect location")
//                .build();
//
//        Mockito.when(teamService.checkGuess(1L, location)).thenReturn(mockResult);
//
//        mockMvc.perform(post("/api/team/1/guess/{location}", location)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isInternalServerError());
//    }
//
//
//    @Test
//    void guessTeamNotFound() throws Exception {
//        Long teamId = 99L;
//
//        // Mock the service to throw exception on delete
//        Mockito.doThrow(new TeamNotFoundException("Team not found")).when(teamService).checkGuess(teamId, "test");
//
//        mockMvc.perform(post("/api/team/{id}/guess/test", teamId)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound()).
//                andExpect(jsonPath("$.message").value("Team not found"));
//    }
//
//    @Test
//    void guessTeamInternalServerError() throws Exception {
//        Long teamId = 4L;
//
//        Mockito.doThrow(new RuntimeException("Database error")).when(teamService).checkGuess(teamId, "test");
//
//        mockMvc.perform(post("/api/team/{id}/guess/test", teamId)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isInternalServerError());
//    }

    @Test
    void guessTeamInvalidIdFormat() throws Exception {
        mockMvc.perform(post("/api/team/{id}/guess/test", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
