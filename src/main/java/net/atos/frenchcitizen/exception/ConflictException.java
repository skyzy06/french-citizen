package net.atos.frenchcitizen.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends FunctionalErrorException {
    public ConflictException(String field, String detail) {
        super(field, detail);
    }
}
