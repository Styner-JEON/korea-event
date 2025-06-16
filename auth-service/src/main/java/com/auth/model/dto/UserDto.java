package com.auth.model.dto;

import com.auth.model.role.UserRole;
import lombok.Getter;

@Getter
public class UserDto {

    private long userId;

    private String username;

    private String password;

    private String email;

    private UserRole userRole;

    private boolean enabled;

}
