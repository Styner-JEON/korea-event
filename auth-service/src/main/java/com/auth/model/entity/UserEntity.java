package com.auth.model.entity;

import com.auth.model.role.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "user_table",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_user_username", columnNames = "username")
    }
)
@NoArgsConstructor
@Getter
public class UserEntity {

    public UserEntity(String email, String username, String password, UserRole userRole) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.userRole = userRole;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String email;

    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    private boolean enabled = true;

}