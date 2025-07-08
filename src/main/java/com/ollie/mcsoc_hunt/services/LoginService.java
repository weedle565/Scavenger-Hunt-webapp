package com.ollie.mcsoc_hunt.services;

import com.ollie.mcsoc_hunt.helpers.JwtGenerator;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    public String login(String username, String password) {

        if (JwtGenerator.checkAdmin(username, password)) return JwtGenerator.generateAdminToken(username);

        return null;
    }

}
