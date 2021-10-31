package net.atos.frenchcitizen.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends FunctionalErrorException {
    public NotFoundException(String field, String detail) {
        super(field, detail);
    }
}
