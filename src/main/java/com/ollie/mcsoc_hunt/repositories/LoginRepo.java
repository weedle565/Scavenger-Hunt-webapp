package com.ollie.mcsoc_hunt.repositories;

import com.ollie.mcsoc_hunt.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginRepo extends JpaRepository<User,  String> {}
