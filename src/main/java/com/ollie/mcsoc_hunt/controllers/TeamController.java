package com.ollie.mcsoc_hunt.controllers;

import com.ollie.mcsoc_hunt.entities.Task;
import com.ollie.mcsoc_hunt.entities.Team;
import com.ollie.mcsoc_hunt.exceptions.TeamNotFoundException;
import com.ollie.mcsoc_hunt.helpers.JwtGenerator;
import com.ollie.mcsoc_hunt.services.TaskService;
import com.ollie.mcsoc_hunt.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/team")
public class TeamController {
    private final TeamService teamService;
    private final TaskService taskService;

    @Autowired
    public TeamController(TeamService teamService, TaskService taskService) {
        this.teamService = teamService;
        this.taskService = taskService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Team>> getAllTeams(@RequestHeader("Authorization") String auth) {
        if (!JwtGenerator.checkAdminJWT(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<Team> teams = teamService.getAllTeams();

        return ResponseEntity.ok(teams);
    }

    @PostMapping
    public ResponseEntity<Team> createTeam(@RequestBody Team team) {
        Map<Boolean, Team> createdTeamMap = teamService.createTeam(team);

        if (createdTeamMap.containsKey(true) && createdTeamMap.get(true) == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (createdTeamMap.containsKey(true)) return ResponseEntity.ok(createdTeamMap.get(true));

        Team teamT = createdTeamMap.get(false);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(teamT.getId())
                .toUri();

        return ResponseEntity.created(location).body(teamT);
    }

    @GetMapping("/{name}/cookie")
    public ResponseEntity<String> generateCooke(@PathVariable String name) {
        String cookie = JwtGenerator.generateToken(name);
        return ResponseEntity.ok(cookie);
    }

    @GetMapping
    public ResponseEntity<Team> getTeamById(@RequestHeader("Authorization") String auth) {
        if (!JwtGenerator.checkJWT(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Team team = teamService.getTeamByCookie(auth);

        if (team == null) throw new TeamNotFoundException("Team not found");

        return ResponseEntity.ok(team);
    }

    @PutMapping("")
    public ResponseEntity<Team> updateTeam(@RequestBody Team teamDetails, @RequestHeader("Authorization") String auth) {

        if (!JwtGenerator.checkAdminJWT(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Team updatedTeam = teamService.updateTeam(auth, teamDetails);

        return ResponseEntity.ok(updatedTeam);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteTeam(@RequestHeader("Authorization") String auth) {
        if (!JwtGenerator.checkAdminJWT(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        teamService.deleteTeam(auth);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeamById(@RequestHeader("Authorization") String auth, @PathVariable Long id) {
        if (!JwtGenerator.checkAdminJWT(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        teamService.deleteTeamById(id);

        return ResponseEntity.noContent().build();
    }


    @GetMapping("/points")
    public ResponseEntity<Integer> getTeamPoints(@RequestHeader("Authorization") String auth) {
        if (!JwtGenerator.checkJWT(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        System.out.println("about to get points btw");

        int points = teamService.getTeamByCookie(auth).getPoints();

        System.out.println(points + " points");

        return ResponseEntity.ok(points); // i think this will work ;-;
    }

    @GetMapping("/complete/{taskId}/{taskType}")
    public ResponseEntity<Integer> completeTask(
            @RequestHeader("Authorization") String auth,
            @PathVariable("taskId") Long taskId,
            @PathVariable("taskType") String taskType) {
        if (!JwtGenerator.checkJWT(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Task task = taskService.getTaskById(taskId, auth);
        Team team = teamService.getTeamByCookie(auth);

        int points = taskType.equals("h") ? task.getHardValue() : task.getEasyValue();
        team.setPoints(team.getPoints() + points);
        team.addTask(task);

        return ResponseEntity.ok(team.getPoints()); // return updated total
    }
}
