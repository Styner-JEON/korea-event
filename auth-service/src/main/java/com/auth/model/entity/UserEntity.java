package com.auth.model.entity;

import com.auth.model.role.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_table")
@NoArgsConstructor
@Getter
public class UserEntity {

    public UserEntity(String email, String password, String username, UserRole userRole) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.userRole = userRole;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String email;

    private String password;

    private String username;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    private boolean enabled = true;

}