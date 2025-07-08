package com.ollie.mcsoc_hunt.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollie.mcsoc_hunt.exceptions.TeamNotFoundException;
import com.ollie.mcsoc_hunt.entities.Team;
import com.ollie.mcsoc_hunt.services.TeamService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Date;
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

        mockMvc.perform(get("/api/team/all")
                        .header("Authorization", "Bearer " + generateAdminTestJWT())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("test team"));
    }

    @Test
    void getTeamTest() throws Exception {
        String jwt = generateTestJWT();

        Mockito.when(teamService.getTeamByCookie(Mockito.anyString())).thenReturn(team);

        mockMvc.perform(get("/api/team")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("test team"));
    }


    @Test
    void getTeamNotFound() throws Exception {

        // Mock the service to throw exception on delete
        Mockito.doThrow(new TeamNotFoundException("Team not found")).when(teamService).getTeamByCookie(generateFakeTestJWT());

        mockMvc.perform(get("/api/team")
                .header("Authorizat" +
                        "ion", "Bearer " + generateFakeTestJWT())
                .contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isNotFound()).
                andExpect(jsonPath("$.message").value("Team not found"));
    }

    @Test
    void getTeamInternalServerError() throws Exception {
        Long teamId = 4L;

        Mockito.doThrow(new RuntimeException("Database error")).when(teamService).getTeamByCookie(generateTestJWT());

        mockMvc.perform(get("/api/team/{id}", teamId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void delTeamsTest() throws Exception {

        Mockito.doNothing().when(teamService).deleteTeam(generateAdminTestJWT());

        mockMvc.perform(delete("/api/team")
                .header("Authorization", "Bearer " + generateAdminTestJWT())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTeamNotFound() throws Exception {
        String fakeJwt = "Bearer " + generateAdminTestJWT();

        // Arrange: mock the service to throw TeamNotFoundException for this JWT string
        Mockito.doThrow(new TeamNotFoundException("Team not found"))
                .when(teamService).deleteTeam(fakeJwt);

        // Act & Assert
        mockMvc.perform(delete("/api/team")
                        .header("Authorization", fakeJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Team not found"));
    }

    @Test
    void deleteTeamInternalServerError() throws Exception {
        Long teamId = 4L;

        Mockito.doThrow(new RuntimeException("Database error")).when(teamService).deleteTeam(generateTestJWT());

        mockMvc.perform(delete("/api/team/{id}", teamId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    private String generateTestJWT() {
        long expirationMs = 86400000;
        return Jwts.builder()
                .setSubject("test team")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs)).signWith(SignatureAlgorithm.HS256, com.ollie.mcsoc_hunt.helpers.JwtGenerator.getSecret().getBytes(StandardCharsets.UTF_8)).compact();
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

    @Test
    void updateTeamTest() throws Exception {
        String jwt = generateAdminTestJWT();
        Team updatedTeam = new Team();
        updatedTeam.setName("new name");
        updatedTeam.setId(1L);

        Mockito.when(teamService.updateTeam(Mockito.anyString(), Mockito.any(Team.class))).thenReturn(updatedTeam);

        mockMvc.perform(put("/api/team")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTeam)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("new name"));
    }

    @Test
    void updateTeamNotFoundTest() throws Exception {
        Mockito.when(teamService.updateTeam(Mockito.anyString(), Mockito.any(Team.class)))
                .thenThrow(new TeamNotFoundException("Team not found"));

        mockMvc.perform(put("/api/team")
                        .header("Authorization", "Bearer " + generateAdminTestJWT())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(team)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Team not found"));
    }

    @Test
    void getTeamUnauthorizedWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/team")
                .header("Authorization", "Bearer boo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTeamUnauthorizedWithoutJwt() throws Exception {
        mockMvc.perform(put("/api/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer boo")
                        .content(objectMapper.writeValueAsString(team)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void generateCookieTest() throws Exception {
        mockMvc.perform(get("/api/team/testteam/cookie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty()); // Or .matches("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\..+$")
    }

    @Test
    void createTeamWithNullNameShouldFail() throws Exception {
        Team invalidTeam = new Team(); // no name set

        // Unauthorised as the created team is null, which is used as the checker for when logins are wrong
        mockMvc.perform(post("/api/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTeam)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTeamInternalServerError() throws Exception {
        Mockito.when(teamService.updateTeam(Mockito.anyString(), Mockito.any(Team.class)))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(put("/api/team")
                        .header("Authorization", "Bearer " + generateAdminTestJWT())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(team)))
                .andExpect(status().isInternalServerError());
    }
}
