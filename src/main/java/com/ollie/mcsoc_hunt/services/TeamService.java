package com.ollie.mcsoc_hunt.services;

import com.ollie.mcsoc_hunt.entities.Team;
import com.ollie.mcsoc_hunt.exceptions.TeamNotFoundException;
import com.ollie.mcsoc_hunt.helpers.TaskHolder;
import com.ollie.mcsoc_hunt.repositories.TeamRepo;
import lombok.Getter;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeamService {

    @Getter
    private final TeamRepo teamRepo;

    TaskHolder holder;


    TeamService(TeamRepo teamRepo) {
        this.teamRepo = teamRepo;
        this.holder = new TaskHolder();
    }

    public List<Team> getAllTeams() {
        List<Team> censoredTeams = new ArrayList<>();

        for (Team team : teamRepo.findAll()) {
            censoredTeams.add(censorPassword(team));
        }

        return censoredTeams;
    }

    public Team createTeam(Team team) {

        for (Team checkingTeam : teamRepo.findAll()) {
            if (checkingTeam.getName().equals(team.getName()) && BCrypt.checkpw(team.getPassword(), checkingTeam.getPassword())) {
                return checkingTeam;
            }
        }

        Team newTeam = new Team();
        newTeam.setName(team.getName());
        newTeam.setId(team.getId());
        newTeam.setCompletedTasks(team.getCompletedTasks());
        newTeam.setRevealedLocations(team.getRevealedLocations());
        newTeam.setPassword(BCrypt.hashpw(team.getPassword(), BCrypt.gensalt()));

        return teamRepo.save(newTeam);
    }

    public Team getTeamById(Long id) {
        Team team = teamRepo.findById(id).orElseThrow(() -> new TeamNotFoundException("Team not found."));

        return censorPassword(team);
    }

    private Team censorPassword(Team team) {
        Team censored = new Team();

        censored.setName(team.getName());
        censored.setId(team.getId());
        censored.setCompletedTasks(team.getCompletedTasks());
        censored.setRevealedLocations(team.getRevealedLocations());
        censored.setPassword("Censored :)");

        return censored;
    }

    public Team updateTeam(Long id, Team teamDetails) {
        Team team = teamRepo.findById(id)
                .orElseThrow(() -> new TeamNotFoundException("Team not found with id: " + id));
        team.setName(teamDetails.getName());
        return teamRepo.save(team);
    }

    public void deleteTeam(Long id) {
        boolean exists = teamRepo.existsById(id);
        if (!exists) throw new TeamNotFoundException("Team not found: " + id);

        teamRepo.deleteById(id);
    }

    public void addGuess(String location, Team team) {
        team.addLocation(location);

        teamRepo.save(team);
    }

}
