package com.citacloud.app.exceptions;

import org.springframework.http.HttpStatus;

public class ValidationException extends ApplicationException {
    public ValidationException(String code, String message, Object details) {
        super(code, message, HttpStatus.BAD_REQUEST, details);
    }
}
