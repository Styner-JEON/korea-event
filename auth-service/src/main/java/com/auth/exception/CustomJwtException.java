package com.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomJwtException extends RuntimeException {

    private final HttpStatus httpStatus;

    public CustomJwtException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

}
