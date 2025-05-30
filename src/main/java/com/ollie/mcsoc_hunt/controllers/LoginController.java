package com.ollie.mcsoc_hunt.controllers;

import com.ollie.mcsoc_hunt.entities.User;
import com.ollie.mcsoc_hunt.helpers.LoginDTO;
import com.ollie.mcsoc_hunt.services.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    private final LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    // Fully remove users capability to add new users to prevent abuse. Only one user is required
//    @PostMapping("/register")
//    public ResponseEntity<User> register(@RequestBody LoginDTO loginDTO) {
//        User user = loginService.register(loginDTO.getUsername(), loginDTO.getPassword());
//        System.out.println(user);
//        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
//                .path("/{id}")
//                .buildAndExpand(user.getUsername())
//                .toUri();
//
//        return ResponseEntity.created(location).body(user);
//    }

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {

        String token = loginService.login(loginDTO.getUsername(), loginDTO.getPassword());


        if (token != null) {
            return ResponseEntity.ok(Map.of("token", token));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorised");

    }

}
