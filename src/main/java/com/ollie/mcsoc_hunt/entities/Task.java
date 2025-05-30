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
        this.riddle = task.riddle;
        this.name = task.name;
        this.description = task.description;
        this.location = task.location;
        this.hasSubtask = task.hasSubtask;
        this.subtaskComplete = task.subtaskComplete;
        this.completed = task.completed;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String riddle;

    @NotEmpty(message = "Task name must be something")
    @Column(unique = true)
    private String name;

    @NotEmpty(message = "Task name must be something")
    private String description;

    @NotEmpty(message = "All tasks must have a location")
    private String location;

    @NotNull
    private Boolean hasSubtask;

    @NotNull
    private Boolean subtaskComplete;

    @NotNull(message = "Must have a completed value")
    private Boolean completed;

    private String subtaskTimeStarted;
    private String subtaskTimeEnding;

    @NotEmpty(message = "Must have a desc")
    private String subtaskDescription;


}
