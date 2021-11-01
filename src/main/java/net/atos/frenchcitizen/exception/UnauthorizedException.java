package net.atos.frenchcitizen.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends FunctionalErrorException {
    public UnauthorizedException(String field, String detail) {
        super(field, detail);
    }
}
