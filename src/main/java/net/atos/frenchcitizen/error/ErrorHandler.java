package net.atos.frenchcitizen.error;

import net.atos.frenchcitizen.exception.FunctionalErrorException;
import net.atos.frenchcitizen.model.Error;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Order
@ControllerAdvice
public class ErrorHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Error methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        Error errorResponse = new Error();
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errorResponse.setField(error.getObjectName());
            errorResponse.setDetail(error.getCode());
        }
        for (FieldError fieldError : fieldErrors) {
            errorResponse.setField(fieldError.getField());
            errorResponse.setDetail(fieldError.getDefaultMessage());
        }
        return errorResponse;
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Error> processRuntimeException(Exception ex) {
        ResponseEntity.BodyBuilder builder;
        Error errorResponse = new Error();
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            builder = ResponseEntity.status(responseStatus.value());
            if (ex instanceof FunctionalErrorException) {
                errorResponse.setField(((FunctionalErrorException) ex).getField());
                errorResponse.setDetail(((FunctionalErrorException) ex).getDetail());
            }
        } else {
            errorResponse.setDetail(ex.getMessage());
            builder = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return builder.body(errorResponse);
    }

}
