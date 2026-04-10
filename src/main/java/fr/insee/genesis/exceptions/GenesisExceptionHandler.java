package fr.insee.genesis.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GenesisExceptionHandler {

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

    /** Returns the corresponding http status, or 500 if the given code does not match a http status. */
    private static HttpStatus resolveHttpCode(int statusCode) {
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);
        return httpStatus != null ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR;
    }
}