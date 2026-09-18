package com.citacloud.app.exceptions;

import org.springframework.http.HttpStatus;

/** Excepción controlada que puede exponerse al cliente de forma segura. */
public class ApplicationException extends RuntimeException {
    private final String code;
    private final HttpStatus status;
    private final Object details;

    public ApplicationException(String code, String message, HttpStatus status) {
        this(code, message, status, null);
    }

    public ApplicationException(String code, String message, HttpStatus status, Object details) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Object getDetails() {
        return details;
    }
}
