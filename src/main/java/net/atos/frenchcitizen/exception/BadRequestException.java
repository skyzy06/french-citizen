package net.atos.frenchcitizen.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends FunctionalErrorException {
    public BadRequestException(String field, String detail) {
        super(field, detail);
    }
}
