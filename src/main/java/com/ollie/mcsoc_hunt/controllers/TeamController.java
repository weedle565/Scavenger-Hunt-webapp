package com.ollie.mcsoc_hunt.controllers;

import com.ollie.mcsoc_hunt.entities.Team;
import com.ollie.mcsoc_hunt.exceptions.TeamNotFoundException;
import com.ollie.mcsoc_hunt.helpers.JwtGenerator;
import com.ollie.mcsoc_hunt.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/team")
public class TeamController {
    private final TeamService teamService;

    @Autowired
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Team>> getAllTeams(@RequestHeader("Authorization") String auth) {
        if (!JwtGenerator.checkAdminJWT(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<Team> teams = teamService.getAllTeams();

        return ResponseEntity.ok(teams);
    }

    @PostMapping
    public ResponseEntity<Team> createTeam(@RequestBody Team team) {
        Team createdTeam = teamService.createTeam(team);
        if (createdTeam == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdTeam.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdTeam);
    }

    @GetMapping("/{name}/cookie")
    public ResponseEntity<String> generateCooke(@PathVariable String name) {
        String cookie = JwtGenerator.generateToken(name);
//        System.out.println(cookie);
        return ResponseEntity.ok(cookie);
    }

    @GetMapping
    public ResponseEntity<Team> getTeamById(@RequestHeader("Authorization") String auth) {
        if (!JwtGenerator.checkJWT(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Team team = teamService.getTeamByCookie(auth);

        System.out.println(team);
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

        System.out.println("In controller");

        teamService.deleteTeam(auth);

        System.out.println("Deleted team");

        return ResponseEntity.noContent().build();
    }

}
