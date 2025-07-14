package com.event.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomEventException extends RuntimeException {

    private final HttpStatus httpStatus;

    public CustomEventException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

}
