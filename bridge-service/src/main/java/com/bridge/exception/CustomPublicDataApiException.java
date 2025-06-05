package com.bridge.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomPublicDataApiException extends RuntimeException {

    private final HttpStatus httpStatus;

    public CustomPublicDataApiException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

}
