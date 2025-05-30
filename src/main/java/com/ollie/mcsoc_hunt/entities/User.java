package com.ollie.mcsoc_hunt.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="users")
@EqualsAndHashCode(callSuper = false)
public class User {

    @Id
    private String username;

    private String password;
}
