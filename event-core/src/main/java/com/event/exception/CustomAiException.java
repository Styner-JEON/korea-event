package com.event.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomAiException extends RuntimeException {

    private final HttpStatus httpStatus;

    public CustomAiException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

}
