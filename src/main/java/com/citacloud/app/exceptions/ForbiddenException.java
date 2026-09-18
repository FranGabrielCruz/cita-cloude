package com.citacloud.app.exceptions;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApplicationException {
    public ForbiddenException() {
        super("FORBIDDEN", "No tienes permisos para realizar esta acción.", HttpStatus.FORBIDDEN);
    }
}
