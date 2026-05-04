package fr.insee.genesis.controller.rest.exception;

import com.mongodb.DuplicateKeyException;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.exceptions.InvalidDateIntervalException;
import fr.insee.genesis.exceptions.NoDataException;
import fr.insee.genesis.exceptions.QuestionnaireNotFoundException;
import fr.insee.genesis.exceptions.ReviewDisabledException;
import fr.insee.genesis.exceptions.SpecificationNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(GenesisException.class)
    public ProblemDetail handleGenesis(GenesisException genesisException) {
        log.error("Genesis error (Type: {}) : {}",
                genesisException.getClass().getSimpleName(),
                genesisException.getMessage(),
                genesisException);

        return ProblemDetail.forStatusAndDetail(
                resolveHttpCode(genesisException.getStatus().value()),
                genesisException.getMessage());
    }

    /** Returns the corresponding http status, or 500 if the given code does not match an http status. */
    private static HttpStatus resolveHttpCode(int statusCode) {
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);
        if (httpStatus == null) {
            log.warn("Unknown http status code '{}', 500 will be sent.", statusCode);
            return  HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return httpStatus;
    }

    @ExceptionHandler(QuestionnaireNotFoundException.class)
    public ProblemDetail handleQuestionnaireNotFound(QuestionnaireNotFoundException questionnaireNotFoundException) {
        log.error("Questionnaire not found (Type: {}) : {}",
                questionnaireNotFoundException.getClass().getSimpleName(),
                questionnaireNotFoundException.getMessage());

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                questionnaireNotFoundException.getMessage());
    }

    @ExceptionHandler(NoDataException.class)
    public ProblemDetail handleNoData(NoDataException exception) {
        log.error("No data found (Type: {}) : {}",
                exception.getClass().getSimpleName(),
                exception.getMessage());

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage());
    }

    @ExceptionHandler(SpecificationNotFoundException.class)
    public ProblemDetail handleSpec(SpecificationNotFoundException exception) {
        log.error("Specifications not available for collectionInstrumentId: {} (Type: {})",
                exception.getCollectionInstrumentId(),
                exception.getClass().getSimpleName());

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage());
    }

    @ExceptionHandler(ReviewDisabledException.class)
    public ProblemDetail handleReviewDisabled(ReviewDisabledException exception) {
        log.error("[{}] {}", exception.getClass().getSimpleName(), exception.getMessage());

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                exception.getMessage());
    }

    @ExceptionHandler(InvalidDateIntervalException.class)
    public ProblemDetail handleInvalidDateIntervalException(InvalidDateIntervalException e) {
        log.error("InvalidDateIntervalException: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                e.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ProblemDetail handleDuplicate(DuplicateKeyException exception) {
        log.error("DuplicateKeyException: {}", exception.getMessage(), exception);

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException exception) {
        log.error("MethodArgumentNotValidException: {}", exception.getMessage());

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        exception.getBindingResult().getGlobalErrors().forEach(error ->
                errors.put(error.getObjectName(), error.getDefaultMessage()));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed");

        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

}
