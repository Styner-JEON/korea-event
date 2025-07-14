package com.event.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomCommentException extends RuntimeException {

    private final HttpStatus httpStatus;

    public CustomCommentException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }
}