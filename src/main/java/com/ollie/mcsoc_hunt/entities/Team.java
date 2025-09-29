package com.ollie.mcsoc_hunt.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="TEAMS")
@EqualsAndHashCode(callSuper = false)
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Password must not be blank")
    private String password;

    @ElementCollection
    private List<String> completedTasks = new ArrayList<>();

    @ElementCollection
    private List<Integer> memberIds;

    @NotBlank(message = "Teams must have 0 points, not null points ;-;")
    private int points;

    public void addTask(Task task) {
        this.completedTasks.add(task.getName());
    }


}