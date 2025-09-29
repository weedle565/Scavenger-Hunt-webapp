package com.ollie.mcsoc_hunt.controllers;

import com.ollie.mcsoc_hunt.helpers.LoginDTO;
import com.ollie.mcsoc_hunt.services.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    private final LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {

        String token = loginService.login(loginDTO.getUsername(), loginDTO.getPassword());


        if (token != null) {
            return ResponseEntity.ok(Map.of("token", token));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorised");

    }

}

/*
Scav 2 planning

Known bugs
Some people get logged out randomly
Not able to join same team after multiple logins
CSS doesn’t load sometimes

New additions
Hold points system internally /
2 types of challenges /
Remove timed challenges /
Make people pick simple vs hard 
Lock hard on completion of challenge
 */