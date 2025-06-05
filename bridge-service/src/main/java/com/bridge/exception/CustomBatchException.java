package com.bridge.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomBatchException extends RuntimeException {

    private final HttpStatus httpStatus;

    public CustomBatchException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

}
