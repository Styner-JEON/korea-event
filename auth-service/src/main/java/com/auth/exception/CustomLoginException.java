package com.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomLoginException extends RuntimeException {

    private final HttpStatus httpStatus;

    public CustomLoginException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

}
