package net.atos.frenchcitizen.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends FunctionalErrorException {
    public ForbiddenException(String field, String detail) {
        super(field, detail);
    }
}
