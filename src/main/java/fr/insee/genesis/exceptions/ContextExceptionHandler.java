package fr.insee.genesis.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;


@ControllerAdvice
public class ContextExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public final ResponseEntity<String> handleInvalidJsonException(HttpMessageNotReadableException ex, WebRequest request) {
        return new ResponseEntity<>("The JSON format sent is invalid or does not match the expected schema.", HttpStatus.BAD_REQUEST);
    }
}