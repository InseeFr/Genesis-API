package fr.insee.genesis.controller.exception;

import fr.insee.genesis.exceptions.*;
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

    // Note: No handler for uncaught Exception.class for now since it breaks soms tests.

    @ExceptionHandler
    public ProblemDetail handleGenericGenesisException(GenesisException genesisException) {
        log.error("GenesisException: {}", genesisException.getMessage(), genesisException);
        return ProblemDetail.forStatusAndDetail(
                resolveHttpCode(genesisException.getStatus()),
                genesisException.getMessage());
    }

    /** Returns the corresponding http status, or 500 if the given code does not match a http status. */
    private static HttpStatus resolveHttpCode(int statusCode) {
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);
        return httpStatus != null ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @ExceptionHandler(InvalidDateIntervalException.class)
    public ProblemDetail handleInvalidDateIntervalException(InvalidDateIntervalException e) {
        log.error("InvalidDateIntervalException: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                e.getMessage());
    }

    @ExceptionHandler(ModesConflictException.class)
    public ProblemDetail handleModesConflictException(ModesConflictException e) {
        log.error("ModesConflictException: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                e.getMessage());
    }

    @ExceptionHandler(UndefinedModesException.class)
    public ProblemDetail handleUndefinedModesException(UndefinedModesException e) {
        log.error("UndefinedModesException: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                e.getMessage());
    }

    @ExceptionHandler(UndefinedMetadataException.class)
    public ProblemDetail handleUndefinedMetadataException(UndefinedMetadataException e) {
        log.error("UndefinedMetadataException: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                e.getMessage());
    }

    @ExceptionHandler(InvalidMetadataException.class)
    public ProblemDetail handleInvalidMetadataException(InvalidMetadataException e) {
        log.error("InvalidMetadataException: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                e.getMessage());
    }

}
