package com.bridge.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomKafkaException extends RuntimeException {

    private final HttpStatus httpStatus;

    public CustomKafkaException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

}
