package fr.insee.genesis.controller.exception;

import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.exceptions.InvalidDateIntervalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * This controller uses Spring's ControllerAdvice annotation to intercept exceptions.
 * It implements the <a href="https://www.rfc-editor.org/rfc/rfc9457.html">RFC 9457</a> by returning
 * Spring's <code>ProblemDetail</code> object.
 */
@ControllerAdvice
@Slf4j
public class ExceptionController {

    // Note: No handler for uncaught Exception.class for now since it breaks some tests.

    @ExceptionHandler
    public ProblemDetail handleGenericGenesisException(GenesisException genesisException) {
        log.error("GenesisException: {}", genesisException.getMessage(), genesisException);
        return ProblemDetail.forStatusAndDetail(
                resolveHttpCode(genesisException.getStatus().value()),
                genesisException.getMessage());
    }

    /** Returns the corresponding http status, or 500 if the given code does not match a http status. */
    private static HttpStatus resolveHttpCode(int statusCode) {
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);
        if (httpStatus == null) {
            log.warn("Unknown http status code '{}', 500 will be sent.", statusCode);
            return  HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return httpStatus;
    }

    @ExceptionHandler(InvalidDateIntervalException.class)
    public ProblemDetail handleInvalidDateIntervalException(InvalidDateIntervalException e) {
        log.error("InvalidDateIntervalException: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                e.getMessage());
    }

}
