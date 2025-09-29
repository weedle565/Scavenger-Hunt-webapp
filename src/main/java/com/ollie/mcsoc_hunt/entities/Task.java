package com.ollie.mcsoc_hunt.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="tasks")
@EqualsAndHashCode(callSuper = false)

public class Task {

    public Task(Task task) {
        this.id = task.id;
        this.name = task.name;
        this.description = task.description;
        this.location = task.location;
        this.completed = task.completed;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Task name must be something")
    @Column(unique = true)
    private String name;

    @NotEmpty(message = "Task name must be something")
    private String description;

    @NotEmpty(message = "All tasks must have a location")
    private String location;

    @NotEmpty(message = "All challenges need an easy challenge")
    private String challenge;

    @NotEmpty(message = "Must have a points value")
    private int easyValue;

    @NotEmpty(message = "All challenges need a hard challenge")
    private String hardChallenge;

    @NotEmpty(message = "Must have a points value")
    private int hardValue;

    @NotNull(message = "Must have a completed value")
    private Boolean completed;


}
