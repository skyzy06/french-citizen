package net.atos.frenchcitizen.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrorException extends FunctionalErrorException {
    public InternalServerErrorException(String field, String detail) {
        super(field, detail);
    }
}
