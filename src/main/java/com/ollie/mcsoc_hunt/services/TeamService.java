package com.ollie.mcsoc_hunt.services;

import com.ollie.mcsoc_hunt.entities.Team;
import com.ollie.mcsoc_hunt.exceptions.TeamNotFoundException;
import com.ollie.mcsoc_hunt.helpers.JwtGenerator;
import com.ollie.mcsoc_hunt.helpers.TaskHolder;
import com.ollie.mcsoc_hunt.repositories.TeamRepo;
import lombok.Getter;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Getter
@Service
public class TeamService {

    private final TeamRepo teamRepo;
    public TeamService(TeamRepo teamRepo) {
        this.teamRepo = teamRepo;
    }

    public List<Team> getAllTeams() {
        return teamRepo.findAll().stream().map(this::censorPassword).toList();
    }

    public Team createTeam(Team team) {
        if (team.getName() == null || team.getPassword() == null) {
            throw new IllegalArgumentException("Name and password must not be null");
        }

        if (checkIfExists(team)) {
            System.out.println(team.getName());
            return login(team);
        }

        Team hashedTeam = new Team();
        hashedTeam.setName(team.getName());
        hashedTeam.setId(team.getId());
        hashedTeam.setPassword(BCrypt.hashpw(team.getPassword(), BCrypt.gensalt()));
        hashedTeam.setCompletedTasks(team.getCompletedTasks());
        hashedTeam.setRevealedLocations(team.getRevealedLocations());

        return teamRepo.save(hashedTeam);
    }

    private boolean checkIfExists(Team team) {
        for (Team teamCheck : teamRepo.findAll()) {
            if (Objects.equals(teamCheck.getName(), team.getName())) return true;
        }

        return false;
    }

    private Team login(Team team) {

        System.out.println(team);
        for (Team teamCheck : teamRepo.findAll()) {
            System.out.println(teamCheck.getPassword());
            if (Objects.equals(teamCheck.getName(), team.getName()) && BCrypt.checkpw(team.getPassword(), teamCheck.getPassword())) return teamCheck;
        }

        return null;
    }

    public Team getTeamById(Long id) {
        return censorPassword(teamRepo.findById(id).orElseThrow(() -> new TeamNotFoundException("Team not found.")));
    }

    public Team getTeamByCookie(String cookie) {
        String teamName = JwtGenerator.extractSubject(cookie);
        return teamRepo.findAll().stream()
                .filter(team -> Objects.equals(team.getName(), teamName))
                .findFirst()
                .map(this::censorPassword)
                .orElseThrow(() -> new TeamNotFoundException("Team not found"));
    }

    public Team updateTeam(String cookie, Team teamDetails) {
        String teamName = JwtGenerator.extractSubject(cookie);
        Team team = teamRepo.findAll().stream()
                .filter(t -> Objects.equals(t.getName(), teamName))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException("Team not found"));

        team.setName(teamDetails.getName());
        return teamRepo.save(team);
    }

    public void deleteTeam(String cookie) {
        String teamName = JwtGenerator.extractSubject(cookie);
        Team team = teamRepo.findAll().stream()
                .filter(t -> Objects.equals(t.getName(), teamName))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException("Team not found"));

        teamRepo.deleteById(team.getId());
    }

    public void addGuess(String location, Team team) {
        team.addLocation(location);
        teamRepo.save(team);
    }

    private Team censorPassword(Team team) {
        Team copy = new Team();
        copy.setId(team.getId());
        copy.setName(team.getName());
        copy.setCompletedTasks(team.getCompletedTasks());
        copy.setRevealedLocations(team.getRevealedLocations());
        copy.setPassword("Censored :)");
        return copy;
    }

    protected Team getByCookie(String cookie) {

        Long user = Long.parseLong(JwtGenerator.extractSubject(cookie));

        Team selectedTeam = null;

        for (Team team : getAllTeams()) {
            if (Objects.equals(team.getId(), user)) selectedTeam = team;
        }

        return selectedTeam;
    }

}
