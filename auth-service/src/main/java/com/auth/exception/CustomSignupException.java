package com.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomSignupException extends RuntimeException {

    private final HttpStatus httpStatus;

    public CustomSignupException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

}