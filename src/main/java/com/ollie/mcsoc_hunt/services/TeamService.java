package com.ollie.mcsoc_hunt.services;

import com.ollie.mcsoc_hunt.entities.Task;
import com.ollie.mcsoc_hunt.entities.Team;
import com.ollie.mcsoc_hunt.exceptions.TeamNotFoundException;
import com.ollie.mcsoc_hunt.helpers.GuessResults;
import com.ollie.mcsoc_hunt.helpers.TaskHolder;
import com.ollie.mcsoc_hunt.repositories.TeamRepo;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.logging.Logger;

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
        return teamRepo.findAll();
    }

    public Team createTeam(Team team) {
//        System.out.println(team);
        return teamRepo.save(team);
    }

    public Team getTeamById(Long id) {
        return teamRepo.findById(id).orElseThrow(() -> new TeamNotFoundException("Team not found."));
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
