package com.ollie.mcsoc_hunt.services;

import com.ollie.mcsoc_hunt.entities.Team;
import com.ollie.mcsoc_hunt.exceptions.TeamNotFoundException;
import com.ollie.mcsoc_hunt.helpers.JwtGenerator;
import com.ollie.mcsoc_hunt.helpers.TaskHolder;
import com.ollie.mcsoc_hunt.repositories.TeamRepo;
import lombok.Getter;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.*;

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

    public Map<Boolean, Team> createTeam(Team team) {
        if (team.getName() == null || team.getPassword() == null) {
            throw new IllegalArgumentException("Name and password must not be null");
        }

        if (checkIfExists(team)) {
            HashMap<Boolean, Team> ret = new HashMap<>();
            ret.put(true, login(team));
            return ret;
        }

        Team hashedTeam = new Team();
        hashedTeam.setName(team.getName());
        hashedTeam.setPassword(BCrypt.hashpw(team.getPassword(), BCrypt.gensalt()));
        hashedTeam.setCompletedTasks(team.getCompletedTasks());

        Team savedTeam = teamRepo.save(hashedTeam);

        HashMap<Boolean, Team> ret = new HashMap<>();
        ret.put(false, savedTeam);

        return ret;
    }

    private boolean checkIfExists(Team team) {
        for (Team teamCheck : teamRepo.findAll()) {
            if (Objects.equals(teamCheck.getName(), team.getName())) return true;
        }

        return false;
    }

    private Team login(Team team) {

        for (Team teamCheck : teamRepo.findAll()) {
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

    public void deleteTeamById(Long id) {
        teamRepo.deleteById(id);
    }

    private Team censorPassword(Team team) {
        Team copy = new Team();
        copy.setId(team.getId());
        copy.setName(team.getName());
        copy.setCompletedTasks(team.getCompletedTasks());
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

    private void addPoints(String cookie, int points) {

        Long user = Long.parseLong(JwtGenerator.extractSubject(cookie));

        Team selectedTeam = null;

        for (Team team : getAllTeams()) {
            if (Objects.equals(team.getId(), user)) selectedTeam = team;
        }
        if (selectedTeam == null) return;

        selectedTeam.setPoints(selectedTeam.getPoints() + points);

    }

}
