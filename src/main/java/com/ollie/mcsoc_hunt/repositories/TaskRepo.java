package com.ollie.mcsoc_hunt.repositories;

import com.ollie.mcsoc_hunt.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepo extends JpaRepository<Task, Long> { }
