package com.ollie.mcsoc_hunt.services;

import com.ollie.mcsoc_hunt.entities.User;
import com.ollie.mcsoc_hunt.helpers.JwtGenerator;
import com.ollie.mcsoc_hunt.repositories.LoginRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCrypt;

@Service
public class LoginService {

    private final LoginRepo loginRepo;

    public LoginService(LoginRepo loginRepo) {
        this.loginRepo = loginRepo;
    }

    public User register(String username, String password) {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());

        User user = new User(username, hash);

        return loginRepo.save(user);

    }

    public String login(String username, String password) {

        User user = loginRepo.findAll().get(0); // still assumes only one admin

        if (user.getUsername().equals(username) && BCrypt.checkpw(password, user.getPassword())) {
            JwtGenerator generator = new JwtGenerator();
            try {
                return generator.generateToken(username);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}
