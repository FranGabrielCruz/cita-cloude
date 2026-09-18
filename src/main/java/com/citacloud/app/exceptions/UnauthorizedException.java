package com.citacloud.app.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApplicationException {
    public UnauthorizedException() {
        super("UNAUTHORIZED", "Tu sesión ha expirado. Inicia sesión nuevamente.", HttpStatus.UNAUTHORIZED);
    }
}
